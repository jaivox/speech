The program here connects a speech recognizer to an SQL database.
Specifically it connects Jaivox libraries to an Sqlite database used
with Mozilla Firefox.

This requires a JDBC driver. We use a driver from Xerial.

The example uses the cookie database for Firefox. It considers three
questions you may ask about the cookies.

a. who has placed a lot of cookies?
b. what portion of cookies is for analytics?
c. what portion is recent?

The program here is generated from db.conf, then modified to use the
SQL interface in dbCommand.java.
