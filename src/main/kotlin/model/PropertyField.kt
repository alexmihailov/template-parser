package model

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import deserializer.TypeEditorDeserializer

/**
 * @author Alex Mihailov {@literal <avmikhaylov@phoenixit.ru>}.
 */
@JsonPropertyOrder(value = ["name", "descRus", "descEng", "type", "example"])
data class PropertyField(
    val name: String,
    val descRus: String,
    val descEng: String,
    @JsonDeserialize(using = TypeEditorDeserializer::class )val type: TypeEditor,
    val example: String?
)
