# Multithreaded Web Crawler

Multithreaded web crawler helps to efficiently crawl websites using multiple threads and get data from the webpages. 

## URLs Graph

This shows an example of URLs graph for a root URL to demonstrate how websites are linked to each other.

<img  alt="An example of URLs Graph" src="https://i.imgur.com/T3BeClx.png">

## Algorithm & Architecture

<img  alt="Multithreaded Architecture Diagram" src="https://i.imgur.com/LULMzOM.png">

## Prerequisites

JRE version >= 19

## Project setup instructions:

1. Clone this repository using command: ``` git clone -b https://github.com/ayushbudh/multithreaded-web-crawler```
2. Navigate inside `src` folder using command: ``` cd ./multithreaded-web-crawler/webCrawler/src```
3. Compile Java program by running command: ```javac  -cp "../lib/jsoup-1.16.2.jar" main/Main.java main/WebCrawler.java main/FileData.java```
4. Run Java program by running command:  ```java -cp "../lib/jsoup-1.16.2.jar;." main.Main```

Note: Please don't run this program using any IDE as it could cause unexpected issues. 

## Links

- Slides: [Google Slides](https://docs.google.com/presentation/d/1c3nzyHG2sK0ZZu-rSNMKiRgsYKzSLjgUsYe_8bW30ro/edit?usp=sharing)

## References

1. Lee, Clara. Https://Www.Cs.Williams.Edu/~cs432/Osco/05-Clara.Pdf. 

2. Haan, Katherine. “Top Website Statistics for 2023.” Forbes, Forbes Magazine, 8 Nov. 2023, www.forbes.com/advisor/business/software/website-statistics/#:~:text=There%20are%20about%201.13%20billion,are%20actively%20maintained%20and%20visited. 

3. What Is a Web Crawler? | How Web Spiders Work | Cloudflare, www.cloudflare.com/learning/bots/what-is-a-web-crawler/. Accessed 28 Nov. 2023. 


4. S. Gupta and K. K. Bhatia, "CrawlPart: Creating Crawl Partitions in Parallel Crawlers," 2013 International Symposium on Computational and Business Intelligence, New Delhi, India, 2013, pp. 137-142, doi: 10.1109/ISCBI.2013.36.

5. K. Vayadande, R. Shaikh, T. Narnaware, S. Rothe, N. Bhavar and S. Deshmukh, "Designing Web Crawler Based on Multi-threaded Approach For Authentication of Web Links on Internet," 2022 6th International Conference on Electronics, Communication and Aerospace Technology, Coimbatore, India, 2022, pp. 1469-1473, doi: 10.1109/ICECA55336.2022.10009614.
