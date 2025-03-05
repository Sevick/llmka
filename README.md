# LangChain4j & Spring-Integration

POC of news deduplicator using transformers

Flow:

config folder -> [config file] -> [news source] -> [news] -> [embedding] -> check for duplicates -> post to telegram channel


First prototype:
- HashTable to fast-check meta for duplicates (cleaned up once in a while removing oldest elements)
- in-memory keystore (saved from time to time as json file)
- blocking similarity check / insertion
