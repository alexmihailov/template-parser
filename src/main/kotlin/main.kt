import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import model.PropertyField
import model.TypeEditor
import java.io.File
import java.util.*

@ExperimentalStdlibApi
fun main(args: Array<String>) {
    val properties = readPropertiesFromCsv(fileName = args[0])
    buildTemplate(properties,
        templateType = args[1],
        templateName = args[2],
        defaultValues = (args.size > 3 && "yes".equals(args[3], ignoreCase = true))
    )
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
    templateName: String,
    defaultValues: Boolean
) {
    val contentItemBuilder = StringJoiner("\n")
    val editorPanelBuilder = StringJoiner("\n")
    val descRuBuilder = StringJoiner("\n")
    val descEnBuilder = StringJoiner("\n")

    properties.forEach {
        val propertyBody = when(it.type) {
            TypeEditor.BOOLEAN -> "<Boolean>${it.example}</Boolean>"
            TypeEditor.STRING, TypeEditor.RICH_TEXT -> {
                if (it.example?.isNotBlank() == true && defaultValues) {
                    if (it.example.contains("<"))
                        "<String><![CDATA[${it.example}]]></String>"
                    else "<String>${it.example}</String>"
                } else "<String/>"
            }
            TypeEditor.LINK_CONTENT -> "<ContentItem type=\"LinkContent\" />"
            else -> "UNKNOWN"
        }

        contentItemBuilder.add("""
        <Property name="${it.name}">
             $propertyBody
        </Property>
        """.trimMargin("|")
        )

        when(it.type) {
            TypeEditor.BOOLEAN, TypeEditor.STRING, TypeEditor.RICH_TEXT -> {
                val propertyPath = "property.${it.name}.label"
                val editorName = when(it.type) {
                    TypeEditor.BOOLEAN -> "BooleanEditor"
                    TypeEditor.STRING -> "StringEditor"
                    TypeEditor.RICH_TEXT -> "RichTextEditor"
                    else -> "UNKNOWN"
                }
                editorPanelBuilder.add("""
            <editors:$editorName propertyName="${it.name}" label="${'$'}{$propertyPath}" />
                """.trimMargin("|")
                )

                descRuBuilder.add("""
                    $propertyPath=${it.descRus}
                """.trimIndent()
                )

                descEnBuilder.add("""
                    $propertyPath=${it.descEng}
                """.trimIndent()
                )
            }
            else -> {}
        }
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

    File("template.xml").writeText(template + "\n")
    File("Resources_ru.properties").writeText(descRuBuilder.toString() + "\n")
    File("Resources_en.properties").writeText(descEnBuilder.toString() + "\n")
}
