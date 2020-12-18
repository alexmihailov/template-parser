@ExperimentalStdlibApi
fun main(args: Array<String>) {
    val properties = readPropertiesFromCsv(fileName = args[0])
    buildTemplate(properties,
        templateType = args[1],
        templateName = args[2],
        defaultValues = (args.size > 3 && "yes".equals(args[3], ignoreCase = true))
    )
}
