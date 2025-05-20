# News aggregator - LangChain4j & Spring-Integration

News aggregator:
- filters similar news (using transformers / semantic similarity search);
- writes a brief for long items;
- determine if last sentence was cut, rewrite if remaining part has enough information or remove;
- filters ads (scoring using LLMes) - not activated;

Demo telegram channel aggregating high-tech news (running some "stable" version): https://t.me/htobserver

Data flow (check [Draw.io diagram](https://github.com/Sevick/llmka/blob/v0.92/doc/llmka_flow_schema.drawio) for more details):

config folder →	[config file] → [newsSource] → [newsData] → run multiple checks → [herald message]

v.1.0:
- HashTable to fast-check meta for duplicates (cleaned up once in a while removing oldest elements)
- in-memory keystore (saved from time to time as json file)
- blocking similarity check / insertion

[LLMka.run.xml](https://github.com/Sevick/llmka/blob/v0.92/LLMka.run.xml) - Idea run configuration (dev)


Monitoring - micrometer, prometheus, grafana
Tracing - zipkin
Logging - log4j2, mapped diagnostic context

Containerized (docker compose atm) deployment was slit into separate repository. Contact me to access.
