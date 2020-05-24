## CI Server Service
Steps to run the application
1. Download project
2. Go into the project directory
3. Run: mvn clean package
4. Run: docker build -t ciserver .
5. Run: docker run -it -p 8080:8080 -v [.ssh path]:/home/testuser/.ssh 
-v [build.sh path]:/home/testuser/build.sh ciserver [repo ssh path] [branch name] /home/testuser/build.sh
6. Check result on http://localhost:8080/bisectionInfo
