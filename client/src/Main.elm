module Main exposing (..)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput, onCheck)
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
  | GotAlarm Alarm

type alias Model =
  { state : State }
  

init : () -> (Model, Cmd Msg)
init _ =
  ( { state = Loading }
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
  | ActiveUpdated Bool


update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    ReceivedAlarm result ->
      case result of
        Ok alarm ->
          ( { model | state = GotAlarm alarm }
          , Cmd.none
          )

        Err error ->
          ( { model | state = Failure error }
          , Cmd.none
          )

    TimeUpdated timeString ->
      case (stringToTime timeString) of
        Ok time ->
          ( { model | state = GotAlarm (SetAlarm time) }
          , postAlarm (SetAlarm time)
          )

        Err _ ->
          ( { model | state = GotAlarm SetAlarmWithInvalidTime }
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
          ( { model | state = GotAlarm SetAlarmWithInvalidTime }
          , Cmd.none
          )

        False -> 
          ( { model | state = GotAlarm UnsetAlarm }
          , postAlarm (UnsetAlarm)
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
    Failure error ->
      [ text "I was unable communicate with the server. "
      , br [] []
      , text ( explainHttpError error )
      ]

    Loading ->
      [ text "Loading..." ]

    GotAlarm alarm ->
        [ makeActiveCheckbox alarm
        , label [ for "isActive" ] [ text " Active " ]
        , br [] []
        , makeTimeInput alarm
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
    Http.header "Authorization" ("Basic " ++ (buildAuthorizationToken username password))

buildAuthorizationToken : String -> String -> String
buildAuthorizationToken username password =
  Base64.encode (username ++ ":" ++ password)
  