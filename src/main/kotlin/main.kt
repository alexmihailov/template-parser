import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import model.PropertyField
import model.TypeEditor
import java.io.File
import java.io.FileReader

@ExperimentalStdlibApi
fun main(args: Array<String>) {
    val properties = readPropertiesFromCsv(fileName = "export.csv")
    val template = buildTemplate(properties, templateType = "B2BSlot", templateName = "B2BSubcriberInfoSlot")
}

@ExperimentalStdlibApi
fun readPropertiesFromCsv(fileName: String): List<PropertyField> {
    val csvMapper = CsvMapper().apply {
        registerModule(KotlinModule())
    }
    val schema = csvMapper.schemaFor(PropertyField::class.java)
        .withHeader()
        .withColumnSeparator(';')
    val iterator: MappingIterator<PropertyField> = csvMapper.readerFor(PropertyField::class.java).with(schema)
        .readValues(File(fileName))
    return buildList {
        while(iterator.hasNextValue()) {
            add(iterator.nextValue())
        }
    }
}

fun buildTemplate(
    properties: List<PropertyField>,
    templateType: String,
    templateName: String
) {
    val contentItemBuilder = StringBuilder()
    val editorPanelBuilder = StringBuilder()
    val descRuBuilder = StringBuilder()
    val descEnBuilder = StringBuilder()

    properties.forEach {
        val propertyBody = when(it.type) {
            TypeEditor.BOOLEAN -> "<Boolean>${it.example}</Boolean>"
            TypeEditor.STRING, TypeEditor.RICH_TEXT -> {
                if (it.example?.contains("<") == true)
                    "<String><![CDATA[${it.example}]]></String>"
                else "<String>${it.example}</String>"
            }
            else -> "UNKNOWN"
        }
        val editorName = when(it.type) {
            TypeEditor.BOOLEAN -> "BooleanEditor"
            TypeEditor.STRING -> "StringEditor"
            TypeEditor.RICH_TEXT -> "RichTextEditor"
            else -> "UNKNOWN"
        }
        val propertyPath = "property.${it.name}.label"

        contentItemBuilder.append("""
            <Property name="${it.name}">
                $propertyBody
            </Property>
            """.trimIndent()
        ).append("\n")

        editorPanelBuilder.append("""
             <editors:$editorName propertyName="${it.name}" label="${'$'}{$propertyPath}" />
        """.trimIndent()
        ).append("\n")

        descRuBuilder.append("""
            $propertyPath=${it.descRus}
        """.trimIndent()
        ).append("\n")

        descEnBuilder.append("""
            $propertyPath=${it.descEng}
        """.trimIndent()
        ).append("\n")
    }

    val template = """
        <?xml version="1.0" encoding="UTF-8"?>
        <ContentTemplate xmlns="http://endeca.com/schema/content-template/2008"
            xmlns:editors="editors"
            xmlns:xavia="http://endeca.com/schema/xavia/2010"
            type="$templateType">
            <Description>${'$'}{template.description}</Description>
            <ThumbnailUrl>thumbnail.jpg</ThumbnailUrl>
            <ContentItem>
                <Name>$templateName</Name>
                $contentItemBuilder
            </ContentItem>
            <EditorPanel>
                <BasicContentItemEditor>
                     $editorPanelBuilder
                </BasicContentItemEditor>
            </EditorPanel>
        </ContentTemplate>
        
    """.trimIndent()

    File("template.xml").writeText(template)
    File("Resources_ru.properties").writeText(descRuBuilder.toString())
    File("Resources_en.properties").writeText(descEnBuilder.toString())
}
