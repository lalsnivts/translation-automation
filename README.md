Инструмент выкачивает фразы для перевода, эмулируя действия пользователя. Для работы необходим пользователь Vkontakte, имеющий доступ к переводам.

1) Собрать при помощи maven командой
`mvn clean compile assembly:single`

Должен получиться файл VKTranslationAutomation-1.0-SNAPSHOT-jar-with-dependencies.jar

2) Скачать http://chromedriver.chromium.org/

3) В vk.com зайти в режим перевода. После этого у вашего пользователя должна корректно открываться страница: http://vk.com/translation

Подробная инструкция в https://vk.com/doc-2288973_462115356

4) Открыть командную строку (в меню "Пуск" выполнить cmd)

5) в открывшемся черном окне набрать
java -jar KTranslationAutomation-1.0-SNAPSHOT-jar-with-dependencies.jar <ваш логин> <ваш пароль> <путь к chromedriver.exe, включая имя файла> <папка, куда будут складываться результаты (должна существовать)> <размер пачки>

Пример:
`java -jar KTranslationAutomation-1.0-SNAPSHOT-jar-with-dependencies.jar "C:\chromedriver_win32\chromedriver.exe" "testlogin@example.com" "password "C:\MyTranslation\resources" "10"`

В папке, указанной как папка для результата, появятся файлы. От размера пачки зависит число строк в файле.

Планы:
* настройки вынести в ini-файл, валидировать их
* сделать возможность не только выгружать переводы, но и загружать сделанные
* <мы открыты предложениям>