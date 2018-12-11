package ru.srcc.minlang;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import ru.srcc.minlang.entity.TranslationKey;
import ru.srcc.minlang.entity.TranslationSection;
import ru.srcc.minlang.exception.AutomationException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class VKTranslationDownloader {
    private String siteLogin;
    private String sitePassword;
    private static final int MAX_SECTION_SIZE = 100;
    private WebDriver driver;

    //setting chrome driver path
    public VKTranslationDownloader(String driverPath, String siteLogin, String sitePassword){
        this.siteLogin = siteLogin;
        this.sitePassword = sitePassword;
        System.setProperty("webdriver.chrome.driver", driverPath);
        driver = new ChromeDriver();
    }
    public void downloadTranslations(int batchSize, String folderName) throws AutomationException {
        try {
            //login to VK.com
            openSite();
            //get a list of all section ids
            List<Integer> sectionIds = getSectionIds();
            //downloading section content in batches
            int from = 0;
            while(from < MAX_SECTION_SIZE) {
                int to = from + batchSize;
                List<TranslationSection> sections = downloadAllSections(sectionIds, from, to);
                String filename = folderName + "\\vk_keys_" + from + "_" + to + ".csv";
                exportSections(sections, filename);
                from = to;
            }
        } catch (Exception e) {
            throw new AutomationException(e.getMessage());
        } finally {
            closeSite();
        }
    }

    private void openSite() {
        //fill in the login, the password, and press the submit button
        driver.get("https://vk.com");
        WebElement emailElement =  driver.findElement(By.id("index_email"));
        emailElement.clear();
        emailElement.sendKeys(siteLogin);
        WebElement passwordElement = driver.findElement(By.id("index_pass"));
        passwordElement.clear();
        passwordElement.sendKeys(sitePassword);
        driver.findElement(By.id("index_login_button")).click();
    }

    private List<TranslationSection>  downloadAllSections(List<Integer> sectionIds,
                                                          int from, int to){
        System.out.println(String.format("Downloading sections from %s to %s", from, to));
        List<TranslationSection> sections = new ArrayList<TranslationSection>();
        int i = 0;
        for(int sectionId : sectionIds){
            if(i >= to){
                break;
            }
            if(i >= from) {
                TranslationSection translationSection = downloadSection(sectionId);
                System.out.println(String.format("Downloaded %s keys for section #%s with id=%s",
                        translationSection.getKeys().size(), i, sectionId));
                sections.add(translationSection);
                if (i % 10 == 0) {
                    System.out.println(String.format("Downloaded %s sections", i));
                }
            }
            ++i;

        }
        return sections;
    }

    private List<Integer> getSectionIds(){
        driver.get("https://vk.com/translation");
        scrollToPageEnd();
        List<Integer> sectionIds = new ArrayList<Integer>();
        List<WebElement> sectionElements = driver.findElements(By.cssSelector("a.ui_rmenu_item"));

        for(WebElement sectionElement : sectionElements){
            try {
                int sectionId = Integer.parseInt(sectionElement.getAttribute("id").split("ui_rmenu_")[1]);
                sectionIds.add(sectionId);
            }
            catch (NumberFormatException e){
                //it's ok since some of the elements have non-numeric ids
            }
        }
        return sectionIds;
    }

    private TranslationSection downloadSection(int sectionNum){
        driver.get("https://vk.com/translation?section=" + sectionNum);
        TranslationSection section = new TranslationSection();
        List<WebElement> sectionNameElements = driver.findElements(By.cssSelector("div.ui_crumb"));
        String sectionName = sectionNameElements.get(0).getAttribute("innerHTML").split("<")[0];
        System.out.println(sectionName);
        section.setSectionName(sectionName);
        scrollToPageEnd();
        List<TranslationKey> keys = downloadSectionKeys();
        section.setKeys(keys);
        return section;
    }

    private List<TranslationKey> downloadSectionKeys(){
        scrollToPageEnd();
        List<WebElement> translationList = driver.findElements(By.cssSelector(".tr_key"));
        List<TranslationKey>alreadyTranslatedKeys = new ArrayList<TranslationKey>();
        List<TranslationKey>notTranslatedKeys = new ArrayList<TranslationKey>();

        System.out.println(String.format("Downloading %s keys ", translationList.size()));
        int i = 0;
        for(WebElement translation : translationList){
            String keyId = translation.findElement(By.cssSelector(".tr_key_name_title")).getText();
            boolean isTranslated = !translation.getAttribute("class").contains("tr_untranslated");
            String sentence = translation.findElement(By.cssSelector(".tr_key_inner")).getText();
            String description = "";
            List<WebElement> descriptionElement = translation.findElements(By.cssSelector(".tr_key_desc"));
            if(!descriptionElement.isEmpty()){
                description = descriptionElement.get(0).getText();
            }
            TranslationKey translationKey = new TranslationKey();
            translationKey.setKeyId(keyId);

            translationKey.setDescription(description);
            translationKey.setTranslated(isTranslated);
            if(isTranslated) {
                translationKey.setTranslationIntoLanguage(sentence);
                alreadyTranslatedKeys.add(translationKey);
            }
            else {
                translationKey.setRussian(sentence);
                notTranslatedKeys.add(translationKey);
            }
            ++i;
            if(i % 10 == 0){
                System.out.println("downloaded " + i + " keys");
            }
        }
        //TODO
        processAlreadyTranslated(alreadyTranslatedKeys);
        List<TranslationKey> keys = alreadyTranslatedKeys;
        keys.addAll(notTranslatedKeys);
        return keys;
    }

    //if the items have already been translated we have to get the Russian text for them, too
    private void processAlreadyTranslated(List<TranslationKey> alreadyTranslatedKeys){
        for(TranslationKey translationKey : alreadyTranslatedKeys){
            driver.get("https://vk.com/translation?lang_id=388&section=&key=" + translationKey.getKeyId());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String parentWindowHandler = driver.getWindowHandle(); // Store your parent window
            String subWindowHandler = null;

            Set<String> handles = driver.getWindowHandles(); // get all window handles
            Iterator<String> iterator = handles.iterator();
            while (iterator.hasNext()){
                subWindowHandler = iterator.next();
            }
            driver.switchTo().window(subWindowHandler); // switch to popup window
            WebElement russianTranslationElement = driver.findElement(By.cssSelector(".tr_value_row"));
            driver.switchTo().window(parentWindowHandler);
            translationKey.setRussian(russianTranslationElement.getText());
        }
    }

    //the page loads automatically on scrolling so we have to load it
    //using https://stackoverflow.com/questions/48850974/selenium-scroll-to-end-of-page-indynamically-loading-webpage
    private void scrollToPageEnd(){
        try {
            Long lastHeight = (Long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");

            while (true) {
                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
                Thread.sleep(2000);

                Long newHeight = (Long) ((JavascriptExecutor) driver).executeScript("return document.body.scrollHeight");
                if (newHeight.compareTo(lastHeight) <= 0) {
                    break;
                }
                lastHeight = newHeight;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void exportSections(List<TranslationSection> sections, String filename) throws AutomationException {
        try (BufferedWriter bw = new BufferedWriter((new OutputStreamWriter
                (new FileOutputStream(filename), StandardCharsets.UTF_8)))){
            bw.write("section name\tsection key\trussian\tis translated\tdescription (if exists)\r\n");
            for(TranslationSection section : sections) {
                for (TranslationKey key : section.getKeys()) {
                    bw.write(section.getSectionName() +
                            "\t" +
                            key.getKeyId() +
                            "\t" +
                            key.getRussian().replaceAll("\\n", "<br>") +
                            "\t" +
                            key.isTranslated() +
                            "\t" +
                            key.getDescription() + "\r\n");
                }
            }
        } catch (IOException e) {
            throw new AutomationException(e.getMessage());
        }

    }

    private void closeSite() {
        driver.quit();
    }

    public static void main(String args[]){


        try{
            if(args.length < 4){
                throw new AutomationException("usage: <login> <password> <path_to_chromediver> <output_folder>");
            }
            System.out.println(Arrays.toString(args));
            String chromeDriverPath = args[0];
            String login = args[1];
            String password = args[2];
            String outputFolderPath= args[3];
            VKTranslationDownloader vkTranslationDownloader =
                    new VKTranslationDownloader(chromeDriverPath,
                            login,
                            password);
            vkTranslationDownloader.downloadTranslations(10,
                    outputFolderPath);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
