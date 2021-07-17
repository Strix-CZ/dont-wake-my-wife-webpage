module Main exposing (..)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput, onCheck, onSubmit)
import Http
import Base64
import Json.Decode exposing (Decoder, field, int, map2, oneOf, bool)
import Json.Encode
import ParseInt
import Array

-- CONFIG

getServerUrl : String
getServerUrl =
  --"http://localhost:8080"
  "https://temer.online/alarm-server"



-- MAIN

main =
  Browser.element
    { init = init
    , update = update
    , subscriptions = subscriptions
    , view = view
    }



-- MODEL

type alias Time =
  { hour : Int
  , minute : Int
  }

type alias Alarm =
  { isActive : Bool
  , time : Time
  }

type State
  = Failure Http.Error
  | Loading
  | Loaded

type alias Model =
  { state : State
  , alarm : Alarm
  , username : String
  , password : String
  }
  

init : () -> (Model, Cmd Msg)
init _ =
  ( { state = Loading
    , alarm = createDefaultAlarm
    , username = ""
    , password = ""
    }
  , (getAlarm "" "")
  )



-- UPDATE

type Msg
  = ReceivedAlarm (Result Http.Error Alarm)
  | AlarmUploaded (Result Http.Error ())
  | TimeUpdated String
  | ActiveUpdated Bool
  | UsernameUpdated String
  | PasswordUpdated String
  | LogIn


update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    ReceivedAlarm result ->
      case result of
        Ok alarm ->
          ( { model | alarm = alarm, state = Loaded }
          , Cmd.none
          )

        Err error ->
          ( { model | state = Failure error }
          , Cmd.none
          )

    TimeUpdated timeString ->
      case (stringToTime timeString) of
        Ok time ->
          let
            alarm = model.alarm
            newAlarm = { alarm | time = time }
            newModel = { model | alarm = newAlarm }
          in
            ( newModel
            , postAlarm newAlarm model.username model.password
            )

        Err _ ->
          ( model
          , Cmd.none
          )

    ActiveUpdated active ->
      let
        alarm = model.alarm
        newAlarm = { alarm | isActive = active }
        newModel = { model | alarm = newAlarm }
      in
        ( newModel
        , postAlarm newAlarm model.username model.password
        )

    UsernameUpdated username ->
      ( { model | username = username}, Cmd.none )

    PasswordUpdated password ->
      ( { model | password = password}, Cmd.none )      

    LogIn ->
      ( model, (getAlarm model.username model.password ) )

    AlarmUploaded result ->
      case result of
        Ok _ ->
          ( model , Cmd.none )

        Err error ->
          ( { model | state = Failure error }
          , Cmd.none
          )



-- SUBSCRIPTIONS

subscriptions : Model -> Sub Msg
subscriptions model =
  Sub.none



-- VIEW

view : Model -> Html Msg
view model =
  div
    [ style "text-align" "center"
    , style "margin" "100px 50px"
    , style "font-family" "Helvetica, sans-serif"
    , style "font-size" "large"
    , style "line-height" "2em"
    ]
    ( viewBody model )

viewBody model =
  case model.state of
    Failure (Http.BadStatus 401) ->
      viewLoginScreen False

    Failure (Http.BadStatus 403) ->
      viewLoginScreen True

    Failure error ->
      [ h1 [] [ text "Error" ]
      , text "I was unable to communicate with the server. "
      , br [] []
      , text ( explainHttpError error )
      ]

    Loading ->
      [ h1 [] [ text "Loading..." ] ]

    Loaded ->
        [ h1 [] [ text "Alarm" ]
        , makeActiveCheckbox model.alarm
        , label [ for "isActive" ] [ text " Active " ]
        , br [] []
        , makeTimeInput model.alarm
        ]

viewLoginScreen : Bool -> List (Html Msg)
viewLoginScreen previousFailed =
  [ h1 [] [ text "Log-in" ]
  , Html.form [ onSubmit LogIn ]
    (
      List.append
      [ input [ type_ "text", placeholder "E-mail", onInput UsernameUpdated ] []
      , br [] []
      , input [ type_ "password", onInput PasswordUpdated ] []
      , br [] []
      , input [ type_ "submit", value "Log-in" ] [ ]
      ]
      (
        case previousFailed of
          False -> []
          True -> [ br [] [], text "Incorrect e-mail or password." ]
      )
    )
  ]


makeActiveCheckbox alarm =
  input
    [ type_ "checkbox"
    , id "isActive"
    , checked alarm.isActive
    , onCheck ActiveUpdated
    , style "width" "2em"
    , style "height" "2em"
    , style "vertical-align" "middle"
    , style "position" "relative"
    , style "bottom" ".08em"
    ]
    []

makeTimeInput alarm =
  input
    [ type_ "time"
    , value (timeToString alarm.time)
    , onInput TimeUpdated
    , style "height" "2em"
    ]
    []

explainHttpError: Http.Error -> String
explainHttpError error =
  case error of
    Http.BadUrl url ->
      "URL " ++ url ++ " is invalid."

    Http.Timeout ->
      "The server did not respond. Are you online?"

    Http.NetworkError ->
      "There was a network error. Are you online?"

    Http.BadStatus status ->
      "The server replied " ++ (String.fromInt status) ++ "."

    Http.BadBody message ->
      "The server responded in an unexpected way. " ++ message



-- JSON

alarmDecoderWithDefault : Decoder Alarm
alarmDecoderWithDefault =
  oneOf
    [ alarmDecoder
    , Json.Decode.succeed createDefaultAlarm
    ]

alarmDecoder : Decoder Alarm
alarmDecoder = 
  Json.Decode.map2 Alarm
    ( field "isActive" bool )
    timeDecoder

timeDecoder : Decoder Time
timeDecoder =
  Json.Decode.map2 Time
    ( field "hour" int )
    ( field "minute" int )

encodeAlarm : Alarm -> Json.Encode.Value
encodeAlarm alarm =
      Json.Encode.object
        [ ( "isActive", Json.Encode.bool alarm.isActive )
        , ( "hour", Json.Encode.int alarm.time.hour )
        , ( "minute", Json.Encode.int alarm.time.minute)
        ]



-- HELPERS

getAlarm : String -> String -> Cmd Msg
getAlarm username password =
  Http.request
    { method = "GET"
    , headers = [(buildAuthorizationHeader username password)]
    , url = (getServerUrl ++ "/alarm")
    , body = Http.emptyBody
    , expect = Http.expectJson ReceivedAlarm alarmDecoderWithDefault
    , timeout = Nothing
    , tracker = Nothing
    }  


-- tavaaziomsaq
postAlarm : Alarm -> String -> String -> Cmd Msg
postAlarm alarm username password =
  Http.request
    { method = "POST"
    , headers = [(buildAuthorizationHeader username password)]
    , url = (getServerUrl ++ "/alarm")
    , body = Http.jsonBody (encodeAlarm alarm)
    , expect = Http.expectWhatever AlarmUploaded
    , timeout = Nothing
    , tracker = Nothing
    }  

timeToString : Time -> String
timeToString time =
  (String.padLeft 2 '0' (String.fromInt time.hour))
    ++ ":"
    ++ (String.padLeft 2 '0' (String.fromInt time.minute))

stringToTime : String -> Result ParseInt.Error Time
stringToTime timeSring = 
  let
    components = String.split ":" timeSring |> Array.fromList
    hourString = Array.get 0 components |> Maybe.withDefault ""
    minuteString = Array.get 1 components |> Maybe.withDefault ""
    hour = ParseInt.parseInt hourString
    minute = ParseInt.parseInt minuteString
  in
    Result.map2 Time hour minute

createDefaultAlarm : Alarm
createDefaultAlarm =
  Alarm False (Time 7 0)

buildAuthorizationHeader : String -> String -> Http.Header
buildAuthorizationHeader username password =
  case (username, password) of
    ("", "") ->
      Http.header "From" "Anonymous"

    _ ->
      Http.header "Authorization" ("Basic " ++ (buildAuthorizationToken username password))

buildAuthorizationToken : String -> String -> String
buildAuthorizationToken username password =
  Base64.encode (username ++ ":" ++ password)
