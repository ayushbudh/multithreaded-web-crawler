# Multithreaded Web Crawler

Multithreaded web crawler helps to efficiently crawl websites using multiple threads and get titles of HTML pages. 

## Algorithm & Architecture

<img  alt="Multithreaded Architecture Diagram" src="https://i.imgur.com/oDCeudh.png">


## Prerequisites

JRE version >= 19

## Project setup instructions:

1. Clone this repository using command: ``` git clone -b https://github.com/ayushbudh/multithreaded-web-crawler```
2. Navigate inside `src` folder using command: ``` cd ./multithreaded-web-crawler/webCrawler/src```
3. Compile Java program by running command: ```javac  -cp "../lib/jsoup-1.16.2.jar" main/Main.java main/WebCrawler.java main/CrawlerStateData.java```
4. Run Java program by running command:  ```java -cp "../lib/jsoup-1.16.2.jar;." main.Main```

Note: Please don't run this program using any IDE as it could cause unexpected issues. 