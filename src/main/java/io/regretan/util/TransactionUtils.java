package io.regretan.util;

import lombok.experimental.UtilityClass;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@UtilityClass
public class TransactionUtils {

  public static void afterCommit(Runnable runnable) {
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            runnable.run();
          }
        }
    );
  }

  public static void beforeCommit(Runnable runnable) {
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void beforeCommit(boolean readOnly) {
            runnable.run();
          }
        }
    );
  }

}
