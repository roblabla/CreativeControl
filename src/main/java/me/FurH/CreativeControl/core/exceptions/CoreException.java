package me.FurH.CreativeControl.core.exceptions;

public class CoreException extends Exception
{
    private static final long serialVersionUID = -7980460792574079663L;
    private String message;
    
    public CoreException(final String message) {
        super(message);
        this.message = message;
    }
    
    public CoreException(final Throwable ex, final String message) {
        super(ex.getMessage(), ex);
        this.message = message;
    }
    
    public String getCoreMessage() {
        return this.message;
    }
    
    public StackTraceElement[] getThreadStackTrace() {
        return Thread.currentThread().getStackTrace();
    }
    
    public StackTraceElement[] getCoreStackTrace() {
        return super.getStackTrace();
    }
    
    public Throwable getCause() {
        return (super.getCause() == null) ? this : super.getCause();
    }
    
    public StackTraceElement[] getStackTrace() {
        return (super.getCause() == null) ? super.getStackTrace() : super.getCause().getStackTrace();
    }
}
