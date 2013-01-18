
(From http://www.jaivox.com/dbappgen.html)

Adding speech recognition to a database

This article explains how you can interface a speech recognizer to an SQL database. A different article explains all the steps involved in connecting any application, including a database application, to a speech recognizer. Here we explain only the database integration steps. In the other example, we add sentences that ar possible questions along with fairly detailed specifications of the application. In this example, most of the database queries are placed directly in the code of a command processor.

An example db illustrates the steps here. This application connects the Jaivox libraries to an Sqlite database (Sqlite is the database used by Mozilla Firefox.) Sqlite is simpler than databases like MySql and Postgres, but we need only the interface to make SQL queries, which works the same way here as in other databases.

To connect a Java program to an Sqlite database, you need to use an appropriate JDBC driver. In this example we use a driver from Xerial..

We use the cookie database for Firefox. We can ask various questions about this database. For this example, we conisder three questions. For each question, we also specify how to handle it. The specification starts with (command, sqlite, to tell the interpreter to send the request to a special command handler. The command handler in this case is SqliteCommand which extends com.jaivox.tools.Command.

a. who has placed a lot of cookies (command, sqlite, mostCookies)
b. what portion of cookies is for analytics (command, sqlite, analyticRatio)
c. what portion is recent (command, sqlite, recentRatio)

In addition to the (command, sqlite, each of the specifications contain the name of a function implemented in SqliteCommand.

For question a, we want to know the number of cookies placed by each domain and then find the one that has the most. This can be done with an SQL query.

select baseDomain, count(*) as frequency rom moz_cookies group by "baseDomain order by count(*) desc

After running this query, we get a list of domains. We can pick the first one in that list and return an anser saying something like "yahoo.com seems to have the most cookies."

Question b involves counting the proportion of analytic cookies. In this case we use the Google Analytics cookies, that may be placed by any site. These cookies have names starting with __utm. To get this proportion, we can query the database using

select name from moz_cookies

After counting the number of cookies overall, we can go through each result to see if it is one of the analytic cookies. After getting this number also, we have create a simple English answer. We do that by seeing whether the ratio of analytic cookies to total cookies is greater than or less than some major ratios. Here we may say "more than three fourths", "more than half" etc, and for a small enough ratio we say "less than a fifth."

The same English formulation is used for Question c. Here we want to find the proportion of recent cookies. For this example, we interpret recent to mean within the last week. To get the proportion, we query the database with

select lastAccessed from moz_cookies

This gives us the access times for all the cookies. The access times in the Firefox database are in microseconds, while Java time is generally given in milliseconds. We can convert the Java time to microseconds by multiplying with 1000. Then we can compare the access times with the microsecond time one week before the current time. Again the proportion can be determined as in the last example and an English sentence can be formulated specifying this proportion.

These questions are just a few examples of the information you can get from a database. You can ask questions involving other fields of the Firefox database by creating additional functions in FindCommand. You can also query information from any other database by replacing the JDBC interface for Sqlite with an interface for another database and modifying the queries appropriately for the database.

(There is a different topic, of querying databases in general in natural language. There has been a lot of work in this area, but we find it best to generate the questions that are relevant to a particular situation, rather than trying to make a general question. The meanings that a user may associate with some data can vary significantly based on the context, and it is better to make something that works than something that is general that does not work.)
