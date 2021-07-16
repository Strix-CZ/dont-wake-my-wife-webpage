module Main exposing (..)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput, onCheck, onSubmit)
import Http
import Base64
import Json.Decode exposing (Decoder, field, int, map2, oneOf)
import Json.Encode
import ParseInt
import Array



-- MAIN

main =
  Browser.element
    { init = init
    , update = update
    , subscriptions = subscriptions
    , view = view
    }



-- MODEL

type alias Time = { hour : Int, minute : Int }

type Alarm
  = UnsetAlarm
  | SetAlarm Time
  | SetAlarmWithInvalidTime

type State
  = Failure Http.Error
  | Loading
  | GotAlarm

type alias Model =
  { state : State
  , alarm : Alarm
  , username : String
  , password : String
  }
  

init : () -> (Model, Cmd Msg)
init _ =
  ( { state = Loading, alarm = UnsetAlarm, username = "", password = "" }
  , (getAlarm "" "")
  )

getAlarm : String -> String -> Cmd Msg
getAlarm username password =
  Http.request
    { method = "GET"
    , headers = [(buildAuthorizationHeader username password)]
    , url = "http://localhost:8080/alarm"
    , body = Http.emptyBody
    , expect = Http.expectJson ReceivedAlarm alarmDecoder
    , timeout = Nothing
    , tracker = Nothing
    }  


-- tavaaziomsaq
postAlarm : Alarm -> String -> String -> Cmd Msg
postAlarm alarm username password =
  Http.request
    { method = "POST"
    , headers = [(buildAuthorizationHeader username password)]
    , url = "http://localhost:8080/alarm"
    , body = Http.jsonBody (encodeAlarm alarm)
    , expect = Http.expectWhatever AlarmUploaded
    , timeout = Nothing
    , tracker = Nothing
    }  



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
          ( { model | alarm = alarm, state = GotAlarm }
          , Cmd.none
          )

        Err error ->
          ( { model | state = Failure error }
          , Cmd.none
          )

    TimeUpdated timeString ->
      case (stringToTime timeString) of
        Ok time ->
          ( { model | alarm = (SetAlarm time) }
          , postAlarm (SetAlarm time) model.username model.password
          )

        Err _ ->
          ( { model | alarm = SetAlarmWithInvalidTime }
          , Cmd.none
          )

    AlarmUploaded result ->
      case result of
        Ok _ ->
          ( model , Cmd.none )

        Err error ->
          ( { model | state = Failure error }
          , Cmd.none
          )

    ActiveUpdated active ->
      case active of
        True ->
          ( { model | alarm = SetAlarmWithInvalidTime }
          , Cmd.none
          )

        False -> 
          ( { model | alarm = UnsetAlarm }
          , (postAlarm (UnsetAlarm) model.username model.password )
          )

    UsernameUpdated username ->
      ( { model | username = username}, Cmd.none )

    PasswordUpdated password ->
      ( { model | password = password}, Cmd.none )      

    LogIn ->
      ( model, (getAlarm model.username model.password ) )


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

    GotAlarm ->
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
    , checked (isAlarmActive alarm)
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
    , value (alarmToString alarm)
    , onInput TimeUpdated
    , style "height" "2em"
    ]
    []

alarmToString : Alarm -> String
alarmToString alarm =
  case alarm of
    UnsetAlarm -> ""
    SetAlarm time -> timeToString time
    SetAlarmWithInvalidTime -> ""

isAlarmActive : Alarm -> Bool
isAlarmActive alarm =
  case alarm of
    UnsetAlarm -> False
    SetAlarm _ -> True
    SetAlarmWithInvalidTime -> True

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

alarmDecoder : Decoder Alarm
alarmDecoder =
  oneOf
    [ alarmSetDecoder
    , Json.Decode.succeed UnsetAlarm
    ]

alarmSetDecoder : Decoder Alarm
alarmSetDecoder = 
  Json.Decode.map SetAlarm (
    Json.Decode.map2 Time
      (field "hour" int)
      (field "minute" int)
    )

encodeAlarm : Alarm -> Json.Encode.Value
encodeAlarm alarm =
  case alarm of
    UnsetAlarm ->
      Json.Encode.null

    SetAlarmWithInvalidTime ->
      Json.Encode.null

    SetAlarm time ->
      Json.Encode.object
        [ ( "hour", Json.Encode.int time.hour )
        , ( "minute", Json.Encode.int time.minute)
        ]


-- HELPERS

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
  