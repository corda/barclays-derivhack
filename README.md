![Corda](https://www.corda.net/wp-content/themes/corda/assets/images/crda-logo-big.svg)

* Pre-requisites
    * This project uses Corda 4.1 to take advantage of reference states
    * As a consequence this project uses Kotlin 1.3
    * This project uses ISDA CDM 2.5.11
    
    For more info see https://docs.corda.net/getting-set-up.html.

* The CDM defines a number of key classes:
    * `Execution` A class to specify an execution, which consists essentially in the economic terms which are agreed between the parties, alongside with the qualification of the type of execution.
    * `Event` A class to specify the lifecycle event.
    * `TransferPrimitive` A class to specify the transfer of assets between parties, those assets being either cash, securities or physical assets.
    * `AllocationPrimitive` A class to specify the primitive event to represent a split/allocation of a trade.
    * `Affirmation` A class to specify a trade affirmation.
    * `Confirmation` A class to specify a trade confirmation.

* Each `Event` has two key fields:
    * `primitive`, defining the type of the event and the changes it causes
    * `lineage`, defining which contracts and other events are related to this event

* There are ten primitive event types, which can live in an `Event` object on their own or together with other primitive events, thus creating more complex events:
    * `Allocation`
    * `ContractFormation`
    * `Execution`
    * `Exercise`
    * `Inception`
    * `Observation`
    * `QuantityChange`
    * `Reset`
    * `TermsChange`
    * `Transfer`


* Mapping CDM on Corda

    The goal we gave ourselves is to stay as close to the CDM events as possible and keep the CDM concepts untouched when persisting CDM events on the ledger
    
    With this in mind here are some basic facts about CDM on Corda
    
    * Each CDM event becomes a Corda `transaction`
    * Each CDM primitive event (i.e. the component of a CDM event) becomes a `command` on this transaction
    * Any product of the CDM event (e.g. contract, payment, observation...) become an output state on this transaction
    * Any contract referred to in the `before` clause of any of the primitive events (e.g. `quantityChange`) is expected to be already stored on the ledger and will become the input state on this transaction
    * Any contract or event referred to under `lineage` of the CDM event becomes a reference state on this transaction
    * The list of parties required to sign this transaction is equal to the set of parties on the contracts, payments, observations (etc.) in this CDM event. Multiple CDM parties can map to same Corda Party. The mapping is maintained in simple wrapped map called `NetworkMap`.
    * Meta information about the CDM event is also output as part of the transaction, in `EventMetadata` output state
    
    In order to create such Corda transaction from a CDM event all the user needs to do is to use the `CdmTransactionBuilder` class. Please refer to unit tests (e.g. cdm-support/src/test/kotlin/net.corda.cdmsupport/testflow/TestFlow.kt) to find out more about how to use it. Essentially it works like a normal `TransactionBuilder` except it takes couple of extra arguments
    
    We also understand that being able to put CDM objects on the ledger without being able to query on them is of little use. Which is why we have created `DefaultCdmVaultQuery`, which exposes such methods as
    
    * getExecutions
    * getAffirmedExecutions
    * getConfirmedExecutions
    * getAffirmations
    * getConfirmations
    
    Please refer to the Derivhack2019.jpeg under doc for a visualisation of the CDM events on Corda
    
* The project has been written entirely in test driven development and the tests in this project are the most accurate documentation of the code. The tests use samples provided by ISDA/Regnosys wherever possible.


### Installation and tech

##### Cloning the project

You can clone the project from here:

https://github.com/corda/barclays-derivhack

##### Load Project in Dev Env - IntelliJ

We recommend loading the project in IntelliJ by either opening the already cloned or by
selecting New -> Project from Version Control and using the link above

##### Running tests inside IntelliJ

We recommend editing your IntelliJ preferences so that you use the Gradle runner - this means that the quasar utils
plugin will make sure that some flags (like ``-javaagent`` - see below) are
set for you.

To switch to using the Gradle runner:

* Navigate to ``Build, Execution, Deployment -> Build Tools -> Gradle -> Runner`` (or search for `runner`)
  * Windows: this is in "Settings"
  * MacOS: this is in "Preferences"
* Set "Delegate IDE build/run actions to gradle" to true
* Set "Run test using:" to "Gradle Test Runner"

If you would prefer to use the built in IntelliJ JUnit test runner, you can run ``gradlew installQuasar`` which will
copy your quasar JAR file to the lib directory. You will then need to specify ``-javaagent:lib/quasar.jar``
and set the run directory to the project root directory for each test.

##### Build project and deploy the nodes

* Run `./gradlew build` command in terminal from the main folder of the project
* Run `./gradlew deployNodes` command after the build has been successful  

##### Running the nodes

NOTE: runnodes.bat isn't a very reliable approach to run the nodes, mostly suitable for testing networks with 2-3 nodes, since in this case its opening up a lot of terminals at times the command to run the node gets executed before the terminal window has opened.
A workaround is to open your terminal, change directory into `build/nodes/<node>`, and run the following command:

```
java -jar corda.jar
```

You will need to run this for each node.

There are currently 5 nodes working with the app that need to be started:

* Client1
* Client2
* Client3
* Broker1
* Broker2
* Notary
* Observery 

For more info see https://docs.corda.net/tutorial-cordapp.html#running-the-example-cordapp.

##### Interacting with the nodes

* Shell

When started via the command line, each node will display an interactive shell:

    Welcome to the Corda interactive shell.
    Useful commands include 'help' to see what is available, and 'bye' to shut down the node.
    
    Tue Oct 01 11:55:05 EEST 2019>>>

You can use this shell to interact with your node. For example, enter `run networkMapSnapshot` to see a list of 
the other nodes on the network:


    Tue Oct 01 11:55:05 EEST 2019>>> run networkMapSnapshot
       [
         { addresses: [ "localhost:10002" ],
           legalIdentitiesAndCerts: [ "O=Notary, L=London, C=GB" ],
           platformVersion: 4,
           serial: 1569919952677
         },
         { addresses: [ "localhost:10005" ],
           legalIdentitiesAndCerts: [ "O=Client1, L=New York, C=US" ],
           platformVersion: 4.
           serial: 1569919955123
         },
         { addresses: [ "localhost:10008" ],
           legalIdentitiesAndCerts: [ "O=Broker1, L=New York, C=US" ],
           platformVersion: 4,
           serial: 1569919994421
         },
         { addresses: [ "localhost:10011" ],
           legalIdentitiesAndCerts: [ "O=Broker2, L=New York, C=US" ],
           platformVersion: 4,
           serial: 1569919954608,
         },
         { addresses: [ "localhost:10014" ],
           legalIdentitiesAndCerts: [ "O=Observery, L=London, C=GB" ],
           platformVersion: 4,
           serial: 1569919956353
         }
    
    Tue Oct 01 11:55:19 EEST 2019>>> 

You can find out more about the node shell [here](https://docs.corda.net/shell.html).

* Webserver

`clients/src/main/kotlin/com/template/webserver/` defines a simple Spring webserver that connects to a node via RPC and 
allows you to interact with the node over HTTP.

The API endpoints are defined here:

     clients/src/main/kotlin/com/template/webserver/Controller.kt 

* Running the webservers via the command line

Run the `runServerB1` Gradle task. By default, it connects to the node with RPC address `localhost:10009` with 
the username `user2` and the password `test`, which is Broker1 Corda Party node, and serves the webserver on port `localhost:10050`.

Run the `runServerC1` Gradle task. By default, it connects to the node with RPC address `localhost:10006` with 
the username `user1` and the password `test`, which is Client1 Corda Party node, and serves the webserver on port `localhost:10060`.

Run the `runServerC2` Gradle task. By default, it connects to the node with RPC address `localhost:10006` with 
the username `user1` and the password `test`, which is Client2 Corda Party node, and serves the webserver on port `localhost:10060`.

Run the `runServerC3` Gradle task. By default, it connects to the node with RPC address `localhost:10006` with 
the username `user1` and the password `test`, which is Client3 Corda Party node, and serves the webserver on port `localhost:10060`.

* Running the webserver via IntelliJ

Run the `Run Server` run configuration. By default, it connects to the node with RPC address `localhost:10009` 
with the username `user2` and the password `test`, which is Broker1 Corda Party node, and serves the webserver on port `localhost:10050`.

Run the `Run Server` run configuration. By default, it connects to the node with RPC address `localhost:10006` 
with the username `user1` and the password `test`, which is Client1 Corda Party node, and serves the webserver on port `localhost:10060`.

* Interacting with the webserver

The static webpage is served on:

    http://localhost:10050 - Broker1
    http://localhost:10060 - Client1
    http://localhost:10070 - Client2
    http://localhost:10080 - Client3


* Processing an Event 

 To process an event like Execution for example you should create a POST request to http://localhost:10050 or http://localhost:10060 depending on which party is the flow initializer with the JSONs provided in "allSampleFiles/2.4.14" ("UC1_block_execute_BT1.json") directory in the project
  to /execution 

* List all execution events

You can get all execution states on http://localhost:10050/execution-states or by running the getExecutions() function from the CdmVaultQuery
    
* Extending the template

You should extend this template as follows:

    * Add your own state and contract definitions under `contracts/src/main/kotlin/`
    * Add your own flow definitions under `workflows/src/main/kotlin/`
    * Extend or replace the client and webserver under `clients/src/main/kotlin/`


​    