package no.mattilsynet.kodekamp2024

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity

data class RequestData(
    val name: String
)

data class ResponseData(
    val message: String
)

@RestController
class KodekampController {
    @GetMapping("/ping")
    fun ping(
        @RequestParam(value = "name", defaultValue = "World")
        name: String
    ): String {
        return "Kodekamp!";
    }

    @PostMapping("/")
    fun post(
        @RequestBody body: RequestData
    ): ResponseEntity<ResponseData> {
        val response = ResponseData(
            message = "Responding!"
        )

        return ResponseEntity.ok(response)
    }
}