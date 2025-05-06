# News aggregator - LangChain4j & Spring-Integration

News aggregator:
	filter similar news (using transformers)
	filter ads (scoring using LLMes

Demo telegram channel aggregating high-tech news: https://t.me/htobserver
*Running some "stable" version.

Data flow (check /doc for drawio schemas):

config folder →	[config file] → [news source] → [news] → run multiple checks → [heraldConfig message]


v.1.0:
- HashTable to fast-check meta for duplicates (cleaned up once in a while removing oldest elements)
- in-memory keystore (saved from time to time as json file)
- blocking similarity check / insertion

LLMka.run.xml - Idea run configuration (dev)

Deployment (including monitoring, tracing) slit into separate repository. Contact me to access.
