package deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.typesafe.config.ConfigFactory
import config.EditorsConfig
import io.github.config4k.extract
import model.TypeEditor

/**
 * @author Alex Mihailov {@literal <avmikhaylov@phoenixit.ru>}.
 */
class TypeEditorDeserializer : JsonDeserializer<TypeEditor>() {

    private val config: EditorsConfig = ConfigFactory.load("application.conf").extract("editorsConfig")

    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): TypeEditor {
        val text = parser.text
        return when {
            text.equals(config.simpleTextDesc, ignoreCase = true) -> TypeEditor.STRING
            text.contains(config.htmlTextDesc, ignoreCase = true) -> TypeEditor.RICH_TEXT
            text.equals(config.booleanEditorDesc, ignoreCase = true) -> TypeEditor.BOOLEAN
            text.equals(config.linkDesc, ignoreCase = true) -> TypeEditor.LINK_CONTENT
            text.equals(config.groupLabelDesc, ignoreCase = true) -> TypeEditor.GROUP_LABEL
            else -> TypeEditor.UNKNOWN
        }
    }
}
