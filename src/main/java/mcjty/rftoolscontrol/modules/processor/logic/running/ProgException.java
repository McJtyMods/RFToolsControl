package mcjty.rftoolscontrol.modules.processor.logic.running;

public class ProgException extends RuntimeException {

    private final ExceptionType exceptionType;

    public ProgException(ExceptionType exceptionType) {
        super(exceptionType.getDescription());
        this.exceptionType = exceptionType;
    }

    public ExceptionType getExceptionType() {
        return exceptionType;
    }
}
