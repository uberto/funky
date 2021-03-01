package com.ubertob.funky.json

import com.ubertob.funky.expectFailure
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ParserFailuresTest {

    @Test
    fun `parsing illegal Int gives us precise errors`() {
        val illegalJson = "123b"

        val error = JInt.fromJson(illegalJson).expectFailure()

        expectThat(error.msg).isEqualTo("error at parsing: Expected an Int at position 1 but found '123b' while parsing <[root]>")
    }

    @Test
    fun `parsing illegal Boolean gives us precise errors`() {
        val illegalJson = "False"

        val error = JBoolean.fromJson(illegalJson).expectFailure()

        expectThat(error.msg).isEqualTo("error at parsing: Expected an Int at position 1 but found '123b' while parsing <[root]>")
    }

    @Test
    fun `parsing illegal String gives us precise errors`() {
        val illegalJson = """
            "unclosed string
            """

        val error = JString.fromJson(illegalJson).expectFailure()

        expectThat(error.msg).isEqualTo("error at parsing: Expected a Strinh at position 1 but found 'EOF' while parsing <[root]>")
    }

    @Test
    fun `parsing illegal Long gives us precise errors`() {
        val illegalJson = "123,234"

        val error = JLong.fromJson(illegalJson).expectFailure()

        expectThat(error.msg).isEqualTo("error at parsing: Expected an Int at position 1 but found '123b' while parsing <[root]>")
    }

    @Test
    fun `parsing illegal json gives us precise errors`() {
        val illegalJson =
            "{\"id\":1001,\"vat-to-pay\":true,\"customer\":{\"id\":1,\"name\":\"ann\"},\"items\":[{\"id\":1001,\"desc\":\"toothpaste \\\"whiter than white\\\"\",\"price:12.34},{\"id\":10001,\"desc\":\"special offer\"}],\"total\":123.45}"

        val error = JInvoice.fromJson(illegalJson).expectFailure()

        expectThat(error.msg).isEqualTo("error at parsing: Expected '\"' at position 7 but found '1001' while parsing <[root]/id>")
    }

    @Test
    fun `parsing json without a field return precise errors`() {
        val jsonWithDifferentField =
            """
 {
  "id": "1001",
  "vat-to-pay": true,
  "customer": {
    "id": 1,
    "name": "ann"
  },
  "items": [
    {
      "id": 1001,
      "short_desc": "toothpaste",
      "long_description": "toothpaste \"whiter than white\"",
      "price": 125
    },
    {
      "id": 10001,
      "short_desc": "special offer"
    }
  ],
  "total": 123.45
}  """.trimIndent()

        val error = JInvoice.fromJson(jsonWithDifferentField).expectFailure()

        expectThat(error.msg).isEqualTo("error at <[root]/items/1>: Not found long_description")
    }


    @Test
    fun `parsing json with different type of fields return precise errors`() {
        val jsonWithDifferentField =
            """
 {
  "id": "1001",
  "vat-to-pay": true,
  "customer": {
    "id": 1,
    "name": "ann"
  },
  "items": [
    {
      "id": 1001,
      "short_desc": "toothpaste",
      "long_description": "toothpaste \"whiter than white\"",
      "price": "a string"
    },
    {
      "id": 10001,
      "short_desc": "special offer"
    }
  ],
  "total": 123.45
}  """.trimIndent()

        val error = JInvoice.fromJson(jsonWithDifferentField).expectFailure()

        expectThat(error.msg).isEqualTo("error at parsing: Expected a Double at position 55 but found '\"' while parsing <[root]/items/0/price>")
    }

    //add tests for... wrong enum, jmap with different node types
}
