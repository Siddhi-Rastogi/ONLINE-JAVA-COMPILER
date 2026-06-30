# Java Online Compiler - Tomcat Deployment Guide

## Project Overview

This is a Java Servlet-based online compiler application that has been migrated from Oracle WebLogic to Apache Tomcat. The application allows users to write, compile, and run Java code through a web interface.

## Technology Stack

- **Java**: JDK 8 or higher
- **Servlet API**: javax.servlet 3.1 (Tomcat 9 compatible)
- **Web Server**: Apache Tomcat 9.0 or 10.x
- **Frontend**: HTML5, CSS3, JavaScript (Vanilla)

## Project Structure

```
JAVA-COMPILER-PROJECT/
├── WebContent/                      # Web application root (WAR content)
│   ├── index.html                   # Main HTML page
│   ├── Myscript.js                  # JavaScript for AJAX communication
│   ├── styles.css                   # Styling
│   ├── Files/                       # User Java source files directory
│   │   ├── *.java                   # Sample Java files
│   │   └── classes/                 # Compiled user classes
│   │       └── *.class
│   └── WEB-INF/                     # Web application configuration
│       ├── web.xml                  # Servlet deployment descriptor
│       ├── classes/                 # Compiled servlet classes
│       │   ├── Compile.class
│       │   ├── Compile$ProcessResult.class
│       │   ├── Compile$StreamCollector.class
│       │   ├── Run.class
│       │   ├── Run$ProcessResult.class
│       │   └── Run$StreamCollector.class
│       └── lib/                     # Library directory (empty)
├── src/                             # Java source files
│   ├── Compile.java                 # Compile servlet source
│   └── Run.java                     # Run servlet source
├── build-tomcat.bat                 # Windows build script
├── Dockerfile                       # Docker configuration
└── DEPLOYMENT.md                    # This file
```

## Key Features Preserved

✅ Java code compilation via AJAX
✅ Java code execution via AJAX
✅ Input validation (class name validation)
✅ Security validation (prevents dangerous operations)
✅ Compiler error display
✅ Runtime output display
✅ All existing functionality maintained

## Servlet Mappings

- **Compile Servlet**: `/Compile` (POST)
  - Compiles Java source code
  - Returns compilation errors or success message
  
- **Run Servlet**: `/Run` (POST)
  - Executes compiled Java classes
  - Returns program output or errors

## Local Deployment on Apache Tomcat

### Prerequisites

1. **Java Development Kit (JDK) 8 or higher**
   - Download from: https://www.oracle.com/java/technologies/downloads/
   - Set `JAVA_HOME` environment variable

2. **Apache Tomcat 9.0 or 10.x**
   - Download from: https://tomcat.apache.org/download-90.cgi or https://tomcat.apache.org/download-10.cgi
   - Extract to a directory (e.g., `C:\Program Files\Apache Software Foundation\Tomcat 9.0`)

### Method 1: Deploy as WAR File (Recommended)

#### Step 1: Build the WAR File

**On Windows:**
```cmd
cd d:\JAVA-COMPILER-PROJECT
build-tomcat.bat
```

This will create `JAVA-COMPILER-PROJECT.war` in the project root.

**On Linux/Mac:**
```bash
cd /path/to/JAVA-COMPILER-PROJECT
# Compile servlets
javac -encoding UTF-8 -cp "$CATALINA_HOME/lib/servlet-api.jar" \
  -d WebContent/WEB-INF/classes src/Compile.java src/Run.java

# Create WAR file
cd WebContent
jar -cvf ../JAVA-COMPILER-PROJECT.war *
cd ..
```

#### Step 2: Deploy to Tomcat

**Option A: Copy to webapps directory**
```cmd
copy JAVA-COMPILER-PROJECT.war "C:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps\"
```

**Option B: Use Tomcat Manager**
1. Start Tomcat
2. Open browser: `http://localhost:8080/manager/html`
3. Login with admin credentials
4. Scroll to "WAR file to deploy" section
5. Select `JAVA-COMPILER-PROJECT.war`
6. Click "Deploy"

#### Step 3: Start Tomcat

```cmd
# Windows
"C:\Program Files\Apache Software Foundation\Tomcat 9.0\bin\startup.bat"

# Linux/Mac
$CATALINA_HOME/bin/startup.sh
```

#### Step 4: Access the Application

Open browser and navigate to:
```
http://localhost:8080/JAVA-COMPILER-PROJECT/
```

Or if deployed as ROOT.war:
```
http://localhost:8080/
```

### Method 2: Deploy as Exploded Directory

#### Step 1: Copy WebContent to Tomcat

```cmd
xcopy /E /I WebContent "C:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps\JAVA-COMPILER-PROJECT\"
```

#### Step 2: Start Tomcat

```cmd
"C:\Program Files\Apache Software Foundation\Tomcat 9.0\bin\startup.bat"
```

#### Step 3: Access the Application

```
http://localhost:8080/JAVA-COMPILER-PROJECT/
```

## Cloud Deployment

### Option 1: Render (Recommended)

Render supports Tomcat applications via Docker or custom buildpacks.

#### Using Docker (Recommended)

1. **Create a `render.yaml` file:**
```yaml
services:
  - type: web
    name: java-compiler
    runtime: docker
    plan: free
    dockerfilePath: ./Dockerfile
    envVars:
      - key: JAVA_TOOL_OPTIONS
        value: -Xmx512m
```

2. **Push to GitHub repository**

3. **Deploy on Render:**
   - Go to https://dashboard.render.com/
   - Click "New" → "Web Service"
   - Connect your GitHub repository
   - Select the repository
   - Render will auto-detect the Dockerfile
   - Click "Create Web Service"

4. **Access your application:**
   - Render will provide a URL like: `https://java-compiler.onrender.com`

#### Using render.yaml (Alternative)

If you prefer not to use Docker, you can use a custom buildpack approach with the existing Dockerfile.

### Option 2: Koyeb

Koyeb is another free platform that supports Docker containers.

1. **Push code to GitHub**

2. **Deploy on Koyeb:**
   - Go to https://app.koyeb.com/
   - Click "Create App"
   - Select "Docker" as deployment method
   - Connect your GitHub repository
   - Select the Dockerfile
   - Choose "Free" plan
   - Click "Deploy"

3. **Access your application:**
   - Koyeb will provide a URL like: `https://java-compiler.koyeb.app`

### Option 3: Docker (Any Platform)

The project includes a production-ready Dockerfile.

#### Build and Run Locally:

```bash
# Build the image
docker build -t java-compiler .

# Run the container
docker run -p 8080:8080 java-compiler

# Access at http://localhost:8080
```

#### Deploy to Any Docker Host:

```bash
# Save image
docker save java-compiler | gzip > java-compiler.tar.gz

# Load on target host
docker load < java-compiler.tar.gz

# Run
docker run -d -p 8080:8080 --name java-compiler java-compiler
```

## Configuration

### Environment Variables

- `JAVA_TOOL_OPTIONS`: JVM options (e.g., `-Xmx512m` for memory)
- `CATALINA_OPTS`: Tomcat-specific JVM options

### Security Considerations

The application includes built-in security validations:

1. **Input Validation:**
   - Class name validation (regex pattern)
   - Source code presence check

2. **Code Security:**
   - Blocks `Runtime.getRuntime()` 
   - Blocks `ProcessBuilder`
   - Blocks `System.exit()`
   - Blocks file deletion operations
   - Blocks network operations (Socket, ServerSocket, URLConnection)

3. **Process Isolation:**
   - Compilation and execution run in separate processes
   - Processes are properly destroyed after use

### File Permissions

Ensure the following directories are writable by Tomcat:
- `Files/` - For storing user Java source files
- `Files/classes/` - For storing compiled classes
- `WEB-INF/classes/` - For servlet classes (read-only after deployment)

## Troubleshooting

### Issue: "JAVA_HOME is not set"

**Solution:** Set JAVA_HOME environment variable to your JDK installation directory.

```cmd
# Windows
setx JAVA_HOME "C:\Program Files\Java\jdk1.8.0_131"

# Linux/Mac
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
```

### Issue: "Could not find servlet-api.jar"

**Solution:** Update the `SERVLET_API` path in `build-tomcat.bat` to point to your Tomcat installation.

```cmd
set "SERVLET_API=C:\Program Files\Apache Software Foundation\Tomcat 9.0\lib\servlet-api.jar"
```

### Issue: Compilation fails with "package javax.servlet does not exist"

**Solution:** Ensure servlet-api.jar is in the classpath during compilation.

### Issue: 404 Error when accessing application

**Solution:** 
- Verify the WAR file is deployed in `webapps/` directory
- Check Tomcat logs: `logs/catalina.out` or `logs/localhost.log`
- Ensure the application name matches the URL path

### Issue: Permission denied when writing files

**Solution:** 
- On Linux/Mac: `chmod -R 777 Files/` (or appropriate permissions)
- On Windows: Run Tomcat as administrator or grant write permissions

## Testing the Deployment

After deployment, verify the following:

1. **Access the main page:**
   - Navigate to `http://localhost:8080/JAVA-COMPILER-PROJECT/`
   - Verify the UI loads correctly

2. **Test Compile functionality:**
   - Enter class name: `Test`
   - Enter code:
     ```java
     public class Test {
         public static void main(String[] args) {
             System.out.println("Hello, World!");
         }
     }
     ```
   - Click "Compile"
   - Verify: "Compiled Successfully" message

3. **Test Run functionality:**
   - Click "Run"
   - Verify output: "Hello, World!"

4. **Test Error Handling:**
   - Enter invalid code:
     ```java
     public class Test {
         public static void main(String[] args) {
             System.out.println("Missing closing brace
         }
     }
     ```
   - Click "Compile"
   - Verify compiler error is displayed

5. **Test Security:**
   - Try to use blocked operations (Runtime, ProcessBuilder, etc.)
   - Verify security messages are displayed

## Maintenance

### Updating the Application

1. Make changes to source files in `src/`
2. Rebuild using `build-tomcat.bat`
3. Redeploy the WAR file to Tomcat
4. Restart Tomcat (or use manager to reload)

### Backup

Regularly backup:
- Source code (`src/`, `WebContent/`)
- User files (`Files/`)
- Configuration files (`web.xml`)

## Migration Notes

### From WebLogic to Tomcat

**Changes Made:**
1. Updated `web.xml` with proper XML schema for Servlet 3.1
2. Organized project structure for standard Tomcat deployment
3. Maintained all existing functionality
4. No changes to Java business logic
5. No changes to user interface
6. No changes to servlet mappings

**What Was NOT Changed:**
- Java servlet code (Compile.java, Run.java)
- Business logic
- Security validations
- AJAX communication
- User interface
- Feature set

**API Compatibility:**
- Using `javax.servlet` API (Servlet 3.1)
- Compatible with Tomcat 9 and 10 (with jakarta namespace for Tomcat 10+)
- No migration to `jakarta.servlet` required for Tomcat 9

## Support

For issues or questions:
1. Check Tomcat logs: `$CATALINA_HOME/logs/`
2. Verify Java version: `java -version`
3. Verify Tomcat version: `http://localhost:8080/docs/`
4. Check browser console for JavaScript errors

## License

[Your License Here]