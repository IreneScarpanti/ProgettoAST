package com.scarpanti.app.playqueue.transaction;

public interface TransactionManager {

	<T> T doInTransaction(TransactionCode<T> any);

}
