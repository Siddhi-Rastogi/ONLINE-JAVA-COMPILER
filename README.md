# Online Java Compiler

A web-based Java compiler that allows users to write, compile, and execute Java programs directly from their browser without installing any IDE or compiler locally.

---

## Features

- Compile Java code in real time
- Execute compiled Java programs
- Display compilation and runtime errors
- Clean and responsive user interface
- AJAX-based communication (no page refresh)
- Input validation
- Basic security validation against dangerous operations
- Docker support for consistent deployment
- Apache Tomcat compatible

---

## Tech Stack

### Backend
- Java
- Java Servlets
- Apache Tomcat 9
- Docker

### Frontend
- HTML5
- CSS3
- JavaScript
- AJAX

---

## Project Architecture

```
Browser
     │
     ▼
HTML + CSS + JavaScript
     │
     ▼
AJAX POST Request
     │
     ▼
Compile Servlet
     │
     ▼
Save Java Source File
     │
     ▼
javac Compiler
     │
     ▼
.class File Generated
     │
     ▼
Run Servlet
     │
     ▼
Java Virtual Machine (JVM)
     │
     ▼
Program Output
     │
     ▼
Browser
```

---

## Project Structure

```
JAVA-COMPILER-PROJECT
│
├── src/
│   ├── Compile.java
│   └── Run.java
│
├── WebContent/
│   ├── index.html
│   ├── Myscript.js
│   ├── styles.css
│   ├── Files/
│   └── WEB-INF/
│
├── Dockerfile
├── README.md
└── .gitignore
```

---

## How It Works

1. User writes Java code in the browser.
2. The code is sent to the Compile Servlet using an AJAX POST request.
3. The servlet saves the source file and invokes the Java compiler.
4. If compilation succeeds, the generated class file is executed by the Run Servlet.
5. The output is returned to the browser without refreshing the page.

---

## Security

- POST requests for code submission
- Input validation
- Java code sanitization
- Blocks dangerous APIs such as:
  - Runtime
  - ProcessBuilder
  - System.exit()
  - File operations
  - Network operations

---

## Docker

Build the image:

```bash
docker build -t java-online-compiler .
```

Run the container:

```bash
docker run -p 8080:8080 java-online-compiler
```

---

## Local Setup

1. Install JDK 17 or above.
2. Install Apache Tomcat 9.
3. Build the project.
4. Deploy the WAR file to Tomcat.
5. Open:

```
http://localhost:8080/
```

---

## Future Improvements

- Automatic cleanup of temporary files
- Execution timeout for infinite loops
- User isolation using Docker containers
- Better logging and monitoring
- Support for multiple programming languages
- Cloud deployment with load balancing

---

## Author

**Siddhi Rastogi**

GitHub: https://github.com/Siddhi-Rastog

