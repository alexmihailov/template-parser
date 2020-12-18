import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.typesafe.config.ConfigFactory
import config.BuilderConfig
import io.github.config4k.extract
import model.PropertyField
import model.TypeEditor
import java.io.File
import java.util.*

/**
 * @author Alex Mihailov {@literal <avmikhaylov@phoenixit.ru>}.
 */

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
        if (it.type != TypeEditor.GROUP_LABEL) {
            contentItemBuilder.add(it.prettyProperty(defaultValues))
        }
        when(it.type) {
            TypeEditor.BOOLEAN, TypeEditor.STRING, TypeEditor.RICH_TEXT -> {
                editorPanelBuilder.add(it.prettyEditorPanel())
                descRuBuilder.add("""${it.getPropertyPath()}=${it.descRus}""".trimIndent())
                descEnBuilder.add("""${it.getPropertyPath()}=${it.descEng}""".trimIndent())
            }
            TypeEditor.GROUP_LABEL -> {
                editorPanelBuilder.add(it.prettyGroupLabel())
                descRuBuilder.add("""${it.getGroupLabel()}=${it.descRus}""".trimIndent())
                descEnBuilder.add("""${it.getGroupLabel()}=${it.descEng}""".trimIndent())
            }
            else -> {}
        }
    }
    val template = createMainBody(templateType, templateName,
        contentItem = contentItemBuilder.toString(),
        editorPanel = editorPanelBuilder.toString()
    )

    val config: BuilderConfig = ConfigFactory.load("application.conf").extract("builderConfig")

    File(config.templateFileName).writeText(template + "\n")
    File(config.resourcesRusFileName).writeText(descRuBuilder.toString() + "\n")
    File(config.resourcesEngFileName).writeText(descEnBuilder.toString() + "\n")
}

private fun TypeEditor.getEditorName() = when(this) {
    TypeEditor.BOOLEAN -> "BooleanEditor"
    TypeEditor.STRING -> "StringEditor"
    TypeEditor.RICH_TEXT -> "RichTextEditor"
    else -> "UNKNOWN"
}

private fun PropertyField.getPropertyBody(defaultValues: Boolean) = when(this.type) {
    TypeEditor.BOOLEAN -> "<Boolean>${this.example}</Boolean>"
    TypeEditor.STRING, TypeEditor.RICH_TEXT -> {
        if (this.example?.isNotBlank() == true && defaultValues) {
            if (this.example.contains("<"))
                "<String><![CDATA[${this.example}]]></String>"
            else "<String>${this.example}</String>"
        } else "<String/>"
    }
    TypeEditor.LINK_CONTENT -> "<ContentItem type=\"LinkContent\" />"
    else -> "UNKNOWN"
}

private fun PropertyField.prettyProperty(defaultValues: Boolean) = """
        <Property name="$name">
            ${this.getPropertyBody(defaultValues)}
        </Property>
""".trimMargin("|")

private fun PropertyField.getPropertyPath() = "property.${this.name}.label"

private fun PropertyField.prettyEditorPanel() = """
            <editors:${this.type.getEditorName()} propertyName="${this.name}" label="${'$'}{${this.getPropertyPath()}}" />
""".trimMargin("|")

private fun PropertyField.getGroupLabel() = "group.${this.name}.label"

private fun PropertyField.prettyGroupLabel() = """
            <GroupLabel label="${'$'}{${this.getGroupLabel()}}" />
""".trimMargin("|")

private fun createMainBody(templateType: String, templateName: String, contentItem: String, editorPanel: String) = """
<?xml version="1.0" encoding="UTF-8"?>
<ContentTemplate xmlns="http://endeca.com/schema/content-template/2008"
    xmlns:editors="editors"
    xmlns:xavia="http://endeca.com/schema/xavia/2010"
    type="$templateType">
    <Description>${'$'}{template.description}</Description>
    <ThumbnailUrl>thumbnail.jpg</ThumbnailUrl>
    <ContentItem>
        <Name>$templateName</Name>
        $contentItem
    </ContentItem>
    <EditorPanel>
        <BasicContentItemEditor>
            $editorPanel
        </BasicContentItemEditor>
    </EditorPanel>
</ContentTemplate>
    """.trimIndent()
