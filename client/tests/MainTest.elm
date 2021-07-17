module MainTest exposing (..)

import Expect exposing (Expectation)
import Fuzz exposing (Fuzzer, int, list, string)
import Test exposing (..)
import Json.Decode
import Main exposing (..)


decodesActiveAlarm : Test
decodesActiveAlarm =
  test "Decodes a set alarm JSON" <|
    \() ->
      let
        input =
          """
          { "alarm":
            { "isActive": true
            , "hour" : 20 
            , "minute" : 4
            }
          }
          """
        decodedOutput =
          Json.Decode.decodeString
            alarmDecoder input
      in
        Expect.equal decodedOutput
          ( Ok (Alarm True
            { hour = 20
            , minute = 4
            }
          ))

decodesUnsetAlarm : Test
decodesUnsetAlarm =
  test "Decodes an unset alarm JSON" <|
    \() ->
      let
        input = """{ "alarm": { } }"""
        decodedOutput =
          Json.Decode.decodeString
            alarmDecoder input
      in
        Expect.equal decodedOutput
          ( Ok (Alarm False (Time 7 0)) )
