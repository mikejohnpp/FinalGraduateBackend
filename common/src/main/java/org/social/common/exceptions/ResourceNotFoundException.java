package org.social.common.exceptions;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super(ErrorCode.RESOURCE_NOT_FOUND, resourceName, id);
    }
}
