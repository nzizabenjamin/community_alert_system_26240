# How to Check Backend Logs

## Method 1: View Logs in Terminal (Recommended)

When you run your Spring Boot application, all logs appear in the terminal/console.

### Step 1: Start the Application
```powershell
mvn spring-boot:run
```

### Step 2: Watch the Console Output
- **All logs** will appear in real-time in the terminal
- Look for lines starting with:
  - `DEBUG:` - Our custom debug messages
  - `ERROR:` - Errors that occurred
  - `WARN:` - Warnings
  - `INFO:` - General information

### Step 3: Filter for Errors (Optional)
If you want to filter the output while running, use:
```powershell
mvn spring-boot:run 2>&1 | Select-String -Pattern "error|Error|ERROR|exception|Exception|DEBUG" -Context 2,2
```

## Method 2: Check Log Files (If Configured)

By default, Spring Boot logs to the console. If you want to save logs to a file, add this to `application.properties`:

```properties
logging.file.name=logs/application.log
```

Then view the log file:
```powershell
Get-Content logs\application.log -Tail 50
```

## What to Look For

### When Issues Page Fails to Load:
Look for these patterns in the logs:

1. **Authentication Issues:**
   ```
   DEBUG: Could not extract user ID from token
   DEBUG: No user authenticated
   ```

2. **Database Errors:**
   ```
   ERROR: org.hibernate.exception.SQLGrammarException
   ERROR: relation "issues" does not exist
   ```

3. **Service Errors:**
   ```
   DEBUG IssueService: Error in getAll(Pageable): [error message]
   Exception type: [exception class]
   ```

4. **Sort Field Errors:**
   ```
   DEBUG: Invalid sort field 'dat...', defaulting to 'dateReported'
   ```

## Common Log Messages

### Successful Request:
```
DEBUG: Loading issues - page: 0, size: 10, sort: dateReported, direction: DESC
DEBUG IssueService: getAll(Pageable) called with sort: dateReported: DESC
DEBUG IssueService: Found 5 total issues
DEBUG: Found 5 issues, returning 5 on this page
```

### Error Example:
```
DEBUG: Loading issues - page: 0, size: 10, sort: dateReported, direction: DESC
DEBUG IssueService: Error in getAll(Pageable): Unable to locate Attribute with the given name [dateReported]
Exception type: org.hibernate.query.sqm.UnknownPathException
Caused by: Unable to locate Attribute with the given name [dateReported]
```

## Tips

1. **Keep the terminal window open** - Don't close it while the app is running
2. **Scroll up** - Errors might have appeared earlier in the output
3. **Look for stack traces** - They show exactly where the error occurred
4. **Check the last 50-100 lines** - Most recent errors are usually at the bottom

## Quick Commands

### View last 50 lines of output:
```powershell
# If you saved output to a file
Get-Content output.log -Tail 50
```

### Search for specific errors:
```powershell
# In PowerShell, while app is running, you can't easily filter
# But you can copy the terminal output and search in a text editor
```

### Restart and watch logs:
```powershell
# Stop the app (Ctrl+C), then:
mvn spring-boot:run
# Watch the console for startup messages and errors
```

