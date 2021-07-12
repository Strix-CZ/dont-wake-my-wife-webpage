module Main exposing (..)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput)
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

type Model
  = Failure Http.Error
  | Loading
  | GotAlarm Alarm

init : () -> (Model, Cmd Msg)
init _ =
  ( Loading
  , getAlarm
  )

getAlarm =
  Http.request
    { method = "GET"
    , headers = [(buildAuthorizationHeader "jan.simonek@gmail.com" "tavaaziomsaq")]
    , url = "http://localhost:8080/alarm"
    , body = Http.emptyBody
    , expect = Http.expectJson ReceivedAlarm alarmDecoder
    , timeout = Nothing
    , tracker = Nothing
    }  

postAlarm : Alarm -> Cmd Msg
postAlarm alarm =
  Http.request
    { method = "POST"
    , headers = [(buildAuthorizationHeader "jan.simonek@gmail.com" "tavaaziomsaq")]
    , url = "http://localhost:8080/alarm"
    , body = Http.jsonBody (encodeAlarm alarm)
    , expect = Http.expectWhatever AlarmUploaded
    , timeout = Nothing
    , tracker = Nothing
    }  



-- UPDATE

type Msg
  = ReceivedAlarm (Result Http.Error Alarm)
  | TimeUpdated String
  | AlarmUploaded (Result Http.Error ())


update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    ReceivedAlarm result ->
      case result of
        Ok alarm ->
          (GotAlarm alarm, Cmd.none)

        Err error ->
          (Failure error, Cmd.none)

    TimeUpdated timeString ->
      case (stringToTime timeString) of
        Ok time ->
          (GotAlarm (SetAlarm time), postAlarm (SetAlarm time))

        Err _ ->
          (GotAlarm SetAlarmWithInvalidTime, Cmd.none)

    AlarmUploaded result ->
      case result of
        Ok _ ->
          (model, Cmd.none)

        Err error ->
          (Failure error, Cmd.none)


-- SUBSCRIPTIONS

subscriptions : Model -> Sub Msg
subscriptions model =
  Sub.none



-- VIEW

view : Model -> Html Msg
view model =
  case model of
    Failure error ->
      text ("I was unable communicate with the server. " ++ (explainHttpError error))

    Loading ->
      text "Loading..."

    GotAlarm alarm ->
      viewInput "time" "time" (alarmToString alarm) TimeUpdated 

alarmToString : Alarm -> String
alarmToString alarm =
  case alarm of
    UnsetAlarm -> ""
    SetAlarm time -> timeToString time
    SetAlarmWithInvalidTime -> ""

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

viewInput : String -> String -> String -> (String -> msg) -> Html msg
viewInput t p v toMsg =
  input [ type_ t, placeholder p, value v, onInput toMsg ] []

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
    Http.header "Authorization" ("Basic " ++ (buildAuthorizationToken username password))

buildAuthorizationToken : String -> String -> String
buildAuthorizationToken username password =
  Base64.encode (username ++ ":" ++ password)
  