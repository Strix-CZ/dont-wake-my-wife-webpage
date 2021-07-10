module Main exposing (..)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput)
import Http
import Base64



-- MAIN


main =
  Browser.element
    { init = init
    , update = update
    , subscriptions = subscriptions
    , view = view
    }



-- MODEL

type Model
  = Failure Http.Error
  | Loading
  | Success String

init : () -> (Model, Cmd Msg)
init _ =
  ( Loading
  , Http.request
        { method = "GET"
        , headers = [(buildAuthorizationHeader "jan.simonek@gmail.com" "tavaaziomsaq")]
        , url = "http://localhost:8080/alarm"
        , body = Http.emptyBody
        , expect = Http.expectString GotAlarm
        , timeout = Nothing
        , tracker = Nothing
        }  
  )



-- UPDATE


type Msg
  = GotAlarm (Result Http.Error String)


update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    GotAlarm result ->
      case result of
        Ok fullText ->
          (Success fullText, Cmd.none)

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
      text ("I was unable to load alarms. " ++ (explainHttpError error))

    Loading ->
      text "Loading..."

    Success fullText ->
      pre [] [ text fullText ]

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
  