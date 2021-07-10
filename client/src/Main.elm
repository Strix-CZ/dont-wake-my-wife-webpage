module Main exposing (..)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput)
import Http
import Base64
import Json.Decode exposing (Decoder, field, int, map2, oneOf)



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

type Model
  = Failure Http.Error
  | Loading
  | GotAlarm Alarm

init : () -> (Model, Cmd Msg)
init _ =
  ( Loading
  , Http.request
        { method = "GET"
        , headers = [(buildAuthorizationHeader "jan.simonek@gmail.com" "tavaaziomsaq")]
        , url = "http://localhost:8080/alarm"
        , body = Http.emptyBody
        , expect = Http.expectJson ReceivedAlarm alarmDecoder
        , timeout = Nothing
        , tracker = Nothing
        }  
  )

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



-- UPDATE


type Msg
  = ReceivedAlarm (Result Http.Error Alarm)
  | TimeUpdated String


update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    ReceivedAlarm result ->
      case result of
        Ok alarm ->
          (GotAlarm alarm, Cmd.none)

        Err error ->
          (Failure error, Cmd.none)

    TimeUpdated time ->
      (model, Cmd.none)


-- SUBSCRIPTIONS

subscriptions : Model -> Sub Msg
subscriptions model =
  Sub.none


-- VIEW

view : Model -> Html Msg
view model =
  case model of
    Failure error ->
      text ("I was unable to load alarms. " ++ (explainHttpError error))

    Loading ->
      text "Loading..."

    GotAlarm alarm ->
      viewInput "time" "time" (alarmToTime alarm) TimeUpdated

alarmToTime : Alarm -> String
alarmToTime alarm =
  case alarm of
    UnsetAlarm -> ""
    SetAlarm time -> timeToString time

timeToString : Time -> String
timeToString time =
  String.fromInt time.hour ++ ":" ++ (String.fromInt time.minute)


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

    Http.BadBody _ ->
      "The server responded in an unexpected way."


--view : Model -> Html Msg
--view model =
--    div [ style "margin" "50px" ]
--        [ viewInput "text" "Name" model.name Name
--        , viewInput "password" "Password" model.password Password
--        , viewInput "password" "Re-enter Password" model.passwordAgain PasswordAgain
--        , viewValidation model
--        ]


--viewInput : String -> String -> String -> (String -> msg) -> Html msg
--viewInput t p v toMsg =
--    div [ style "padding" "10px" ]
--        [ input [ type_ t, placeholder p, value v, onInput toMsg ] []
--        ]


--viewValidation : Model -> Html msg
--viewValidation model =
--    if model.password == model.passwordAgain then
--        div [ style "color" "green" ] [ text "OK" ]
--    else
--        div [ style "color" "red" ] [ text "Passwords do not match!" ]


buildAuthorizationHeader : String -> String -> Http.Header
buildAuthorizationHeader username password =
    Http.header "Authorization" ("Basic " ++ (buildAuthorizationToken username password))

buildAuthorizationToken : String -> String -> String
buildAuthorizationToken username password =
  Base64.encode (username ++ ":" ++ password)
  