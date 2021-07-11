module MainTest exposing (..)

import Expect exposing (Expectation)
import Fuzz exposing (Fuzzer, int, list, string)
import Test exposing (..)
import Json.Decode
import Main exposing (..)


decodesSetAlarm : Test
decodesSetAlarm =
  test "Decodes a set alarm JSON" <|
    \() ->
      let
        input =
          """
          { "hour" : 20 
          , "minute" : 4
          }
          """
        decodedOutput =
          Json.Decode.decodeString
            alarmDecoder input
      in
        Expect.equal decodedOutput
          ( Ok (SetAlarm
            { hour = 20
            , minute = 4
            }
          ))

decodesUnsetAlarm : Test
decodesUnsetAlarm =
  test "Decodes an unset alarm JSON" <|
    \() ->
      let
        input = "{}"
        decodedOutput =
          Json.Decode.decodeString
            alarmDecoder input
      in
        Expect.equal decodedOutput
          ( Ok UnsetAlarm )
