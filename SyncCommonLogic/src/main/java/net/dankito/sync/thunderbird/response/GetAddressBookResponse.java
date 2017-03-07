package net.dankito.sync.thunderbird.response;

import net.dankito.sync.ContactSyncEntity;
import net.dankito.sync.communication.message.Response;
import net.dankito.sync.communication.message.ResponseErrorType;

import java.util.List;


public class GetAddressBookResponse extends Response<List<ContactSyncEntity>> {

  public GetAddressBookResponse(ResponseErrorType errorType, Exception error) {
    super(errorType, error);
  }

  public GetAddressBookResponse(List<ContactSyncEntity> body) {
    super(body);
  }

}
