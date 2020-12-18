package deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import model.TypeEditor

/**
 * @author Alex Mihailov {@literal <avmikhaylov@phoenixit.ru>}.
 */
class TypeEditorDeserializer : JsonDeserializer<TypeEditor>() {

    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): TypeEditor {
        val text = parser.text
        return when {
            text.equals(SIMPLE_TEXT, ignoreCase = true) -> TypeEditor.STRING
            text.contains(HTML_TEXT, ignoreCase = true) -> TypeEditor.RICH_TEXT
            text.equals(BOOLEAN_EDITOR, ignoreCase = true) -> TypeEditor.BOOLEAN
            text.equals(LINK, ignoreCase = true) -> TypeEditor.LINK_CONTENT
            text.equals(GROUP_LABEL, ignoreCase = true) -> TypeEditor.GROUP_LABEL
            else -> TypeEditor.UNKNOWN
        }
    }
}
