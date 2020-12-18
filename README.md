#Запуск:

```shell
java -jar template-parser.jar  arg0 arg1 arg2 arg3
```

* arg0 - Путь к исходному csv файлу
* arg1 - Тип картриджа
* arg2 - Название картриджа
* arg3 - Использовать значения по умолчанию ("yes/no"), необязательный параметр

На выходе три файла:

* template.xml - файл с разметкой картриджа
* Resources_ru.properties - тексты в русской локали
* Resources_en.properties - тексты в английской локали

Если не смогло распарсить, то будет указано UNKNOWN
