 gob.datos.consumer
 ================
 
Consume landings data from datos.gob.ar and store them raw in our database.
 
 Prerequisites
 =============
 
 - java 8
 - Docker Desktop 2.1.7.0
 
 
 Run
 ===
 
 Add this line to your `/etc/hosts` file:
 
 ```
 127.0.0.1   postgres
 ```
 
 Inside the root directory, run the postgres database on docker:
 
 ```bash
 docker-compose up
 ```
 
 then:
 
 ```bash
./sbt
 ```
 
 and once inside the `sbt` shell:
 
 ```bash
 ~reStart
 ```
 
 it will listen for code changes and restart the server automatically.
 
 
 Debug
 =====
 
 To manually query the postgres database:
 
 ```bash
 docker exec -it postgres bash
 ```
 
 then, login to the database:
 
 ```bash
 psql -d postgres -U postgres
 ```
 
 now, you can run any query. Start with `\dt` 
 
 
 Contribute
 ==========
 
 1) Branch off from master
 2) Code
 3) `sbt scalafmtAll` to format the code before pushing.
 4) Write [good commit messages](https://github.com/erlang/otp/wiki/writing-good-commit-messages).
     Please reference the issue number in the commit message description
 5) PR to master
 6) Push force to your branch if you have to make changes
 7) If you have to update your branch with master, please `git rebase master` from your branch, and force push.
     Please do not merge master to your branch.
 8) Rebase merge into master
