Ticker Tracker revolves around the ability to keep track of stock prices of NYSE scrips

- Users can check the stock price of a scrip (listed on NASDAQ) using a simple REST interface
- Real time price tracking is also available – but this is only for Oracle (ORCL)

It's a simple fat JAR app built using 

- Java EE 7: JAX-RS, WebSocket, EJB, CDI, JSON-P 
- Wildfly Swarm

### To run

- First `git clone https://github.com/abhirockzz/wildfly-swarm-javaee-stock-tracker`
- `cd <code_dir>`
- `mvn clean install`
- `cd <code_dir>/target`
- `java -jar stock-tracker-swarm.jar`

### To test

- check of a specific ticker `http://localhost:8080/api/stocks?ticker=AAPL`
- track ORCL stock `ws://localhost:8080/rt/stocks`
