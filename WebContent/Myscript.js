function getElement(id) {
    return document.getElementById(id);
}

function classNameValue() {
    return getElement("class").value.trim();
}

function isValidClassName(className) {
    return /^[A-Za-z_$][A-Za-z0-9_$]*$/.test(className);
}

function showOutput(message) {
    getElement("code1").value = message;
}

function encodeFormData(data) {
    var parts = [];
    for (var key in data) {
        if (data.hasOwnProperty(key)) {
            parts.push(encodeURIComponent(key) + "=" + encodeURIComponent(data[key]));
        }
    }
    return parts.join("&");
}

function sendPost(url, data, onSuccess) {
    var request = window.XMLHttpRequest
        ? new XMLHttpRequest()
        : new ActiveXObject("Microsoft.XMLHTTP");

    request.onreadystatechange = function() {
        if (request.readyState === 4) {
            if (request.status === 200) {
                onSuccess(request.responseText);
            } else {
                showOutput("Request failed. Server returned status " + request.status + ".");
            }
        }
    };

    request.open("POST", url, true);
    request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
    request.send(encodeFormData(data));
}

function validateClassName() {
    var className = classNameValue();
    if (className.length === 0) {
        showOutput("Please enter a Java class name.");
        getElement("class").focus();
        return null;
    }
    if (!isValidClassName(className)) {
        showOutput("Class name must be a valid Java identifier, for example Main or StudentDemo.");
        getElement("class").focus();
        return null;
    }
    return className;
}

function set() {
    var className = classNameValue();
    var codeEditor = getElement("code");

    if (className.length === 0 || !isValidClassName(className) || codeEditor.value.trim().length > 0) {
        return;
    }

    codeEditor.value =
        "public class " + className + " {\n" +
        "    public static void main(String[] args) {\n" +
        "        System.out.println(\"Hello, World!\");\n" +
        "    }\n" +
        "}\n";
}

function compile() {
    var className = validateClassName();
    var sourceCode = getElement("code").value;

    if (className === null) {
        return;
    }

    if (sourceCode.trim().length === 0) {
        showOutput("Please enter Java source code before compiling.");
        getElement("code").focus();
        return;
    }

    showOutput("Compiling...");
    sendPost("Compile", {
        className: className,
        code: sourceCode
    }, showOutput);
}

function run() {
    var className = validateClassName();

    if (className === null) {
        return;
    }

    showOutput("Running...");
    sendPost("Run", {
        className: className
    }, showOutput);
}

function blank() {
    getElement("class").value = "";
    getElement("code").value = "";
    showOutput("");
    getElement("class").focus();
}
