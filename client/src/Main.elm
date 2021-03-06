module Main exposing (..)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput, onCheck, onSubmit)
import Http
import Base64
import Json.Decode exposing (Decoder, field, int, map2, oneOf, bool, string)
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
  | Saving

type alias CheckIn =
  { time : String
  , battery : Int
  }

type alias Model =
  { state : State
  , alarm : Alarm
  , checkIn : Maybe CheckIn
  , username : String
  , password : String
  }
  

init : () -> (Model, Cmd Msg)
init _ =
  ( { state = Loading
    , alarm = createDefaultAlarm
    , checkIn = Nothing
    , username = ""
    , password = ""
    }
  , (getAlarmAndCheckIn "" "")
  )



-- UPDATE

type Msg
  = ReceivedAlarmAndCheckIn (Result Http.Error (Alarm, Maybe CheckIn))
  | AlarmUploaded (Result Http.Error ())
  | TimeUpdated String
  | ActiveUpdated Bool
  | UsernameUpdated String
  | PasswordUpdated String
  | LogIn


update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    ReceivedAlarmAndCheckIn result ->
      case result of
        Ok (alarm, checkIn) ->
          ( { model | alarm = alarm, checkIn = checkIn, state = Loaded }
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
            newModel = { model | alarm = newAlarm, state = Saving }
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
        newModel = { model | alarm = newAlarm, state = Saving }
      in
        ( newModel
        , postAlarm newAlarm model.username model.password
        )

    UsernameUpdated username ->
      ( { model | username = username}, Cmd.none )

    PasswordUpdated password ->
      ( { model | password = password}, Cmd.none )      

    LogIn ->
      ( model, (getAlarmAndCheckIn model.username model.password ) )

    AlarmUploaded result ->
      case result of
        Ok _ ->
          ( { model | state = Loaded }
          , Cmd.none
          )

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
    , style "font-size" "x-large"
    , style "line-height" "2em"
    ]
    ( viewBody model )

viewBody : Model -> List (Html Msg)
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
      viewLoadedModel model

    Saving ->
      viewLoadedModel model
        

viewLoadedModel : Model -> List (Html Msg)
viewLoadedModel model =
  List.append
  [ h1 [] [ text "Alarm" ]
  , makeActiveCheckbox model.alarm
  , label [ for "isActive" ] [ text " Active " ]
  , br [] []
  , makeTimeInput model.alarm
  , br [] []
  , viewCheckIn model.checkIn
  ]
  (viewSavingAnimation model)

viewSavingAnimation : Model -> List (Html Msg)
viewSavingAnimation model =
  case model.state of
    Saving ->
      [ h3 [] [ text "Saving..." ] ]

    _ -> []

viewLoginScreen : Bool -> List (Html Msg)
viewLoginScreen previousFailed =
  [ h1 [] [ text "Log-in" ]
  , Html.form [ onSubmit LogIn ]
    (
      List.append
      [ input (styleInput [ type_ "text", placeholder "E-mail", onInput UsernameUpdated ]) []
      , br [] []
      , input ( styleInput [ type_ "password", onInput PasswordUpdated ]) []
      , br [] []
      , input (styleInput [ type_ "submit", value "Log-in" ]) [ ]
      ]
      (
        case previousFailed of
          False -> []
          True -> [ br [] [], text "Incorrect e-mail or password." ]
      )
    )
  ]

styleInput : List (Attribute msg) -> List (Attribute msg)
styleInput attributes =
  List.append attributes 
  [ style "font-size" "large"
  , style "padding" "0.4em"
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
    , style "padding" "0.8em"
    , style "font-size" "x-large"
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


viewCheckIn : Maybe CheckIn -> Html Msg
viewCheckIn maybeCheckIn =
  div 
    [ style "font-size" "medium"
    , style "margin-top" "1em"
    ]
    [
      case maybeCheckIn of
        Nothing ->
          text "The device has never been on-line."

        Just checkIn ->
          text ("Device was last on-line at "
            ++ checkIn.time
            ++ " with battery level "
            ++ (String.fromInt checkIn.battery)
            ++ ".")
    ]

-- JSON

alarmAndCheckInDecoder : Decoder (Alarm, Maybe CheckIn)
alarmAndCheckInDecoder =
  map2 Tuple.pair
    alarmDecoder
    checkInDecoder

alarmDecoder : Decoder Alarm
alarmDecoder =
  field "alarm" (
    oneOf
      [ Json.Decode.map2 Alarm
        ( field "isActive" bool )
        timeDecoder
      , Json.Decode.succeed createDefaultAlarm
      ]
  )

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

checkInDecoder : Decoder (Maybe CheckIn)
checkInDecoder =
  field "checkIns" (
      Json.Decode.maybe (
        Json.Decode.index 0 (
            Json.Decode.map2 CheckIn
              (field "time" string)
              (field "battery" int)
        )
      )
    )

-- HELPERS

getAlarmAndCheckIn : String -> String -> Cmd Msg
getAlarmAndCheckIn username password =
  Http.request
    { method = "GET"
    , headers = [(buildAuthorizationHeader username password)]
    , url = (getServerUrl ++ "/alarm")
    , body = Http.emptyBody
    , expect = Http.expectJson ReceivedAlarmAndCheckIn alarmAndCheckInDecoder
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
