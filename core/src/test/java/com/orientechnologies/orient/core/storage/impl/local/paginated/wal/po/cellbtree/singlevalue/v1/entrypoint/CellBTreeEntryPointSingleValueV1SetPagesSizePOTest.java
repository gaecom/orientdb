package com.orientechnologies.orient.core.storage.impl.local.paginated.wal.po.cellbtree.singlevalue.v1.entrypoint;

import com.orientechnologies.common.directmemory.OByteBufferPool;
import com.orientechnologies.common.directmemory.OPointer;
import com.orientechnologies.orient.core.storage.cache.OCacheEntry;
import com.orientechnologies.orient.core.storage.cache.OCacheEntryImpl;
import com.orientechnologies.orient.core.storage.cache.OCachePointer;
import com.orientechnologies.orient.core.storage.impl.local.paginated.wal.po.PageOperationRecord;
import com.orientechnologies.orient.core.storage.index.sbtree.singlevalue.v1.CellBTreeSingleValueEntryPointV1;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CellBTreeEntryPointSingleValueV1SetPagesSizePOTest {
  @Test
  public void testRedo() {
    final int pageSize = 64 * 1024;
    final OByteBufferPool byteBufferPool = new OByteBufferPool(pageSize);
    try {
      final OPointer pointer = byteBufferPool.acquireDirect(false);
      final OCachePointer cachePointer = new OCachePointer(pointer, byteBufferPool, 0, 0);
      final OCacheEntry entry = new OCacheEntryImpl(0, 0, cachePointer, false);

      CellBTreeSingleValueEntryPointV1 bucket = new CellBTreeSingleValueEntryPointV1(entry);
      bucket.init();

      bucket.setPagesSize(42);

      entry.clearPageOperations();

      final OPointer restoredPointer = byteBufferPool.acquireDirect(false);
      final OCachePointer restoredCachePointer =
          new OCachePointer(restoredPointer, byteBufferPool, 0, 0);
      final OCacheEntry restoredCacheEntry = new OCacheEntryImpl(0, 0, restoredCachePointer, false);

      final ByteBuffer originalBuffer = cachePointer.getBufferDuplicate();
      final ByteBuffer restoredBuffer = restoredCachePointer.getBufferDuplicate();

      Assert.assertNotNull(originalBuffer);
      Assert.assertNotNull(restoredBuffer);

      restoredBuffer.put(originalBuffer);

      bucket.setPagesSize(24);

      final List<PageOperationRecord> operations = entry.getPageOperations();
      Assert.assertEquals(1, operations.size());

      Assert.assertTrue(
          operations.get(0) instanceof CellBTreeEntryPointSingleValueV1SetPagesSizePO);

      final CellBTreeEntryPointSingleValueV1SetPagesSizePO pageOperation =
          (CellBTreeEntryPointSingleValueV1SetPagesSizePO) operations.get(0);

      CellBTreeSingleValueEntryPointV1 restoredBucket =
          new CellBTreeSingleValueEntryPointV1(restoredCacheEntry);
      Assert.assertEquals(42, restoredBucket.getPagesSize());

      pageOperation.redo(restoredCacheEntry);

      Assert.assertEquals(24, restoredBucket.getPagesSize());

      byteBufferPool.release(pointer);
      byteBufferPool.release(restoredPointer);
    } finally {
      byteBufferPool.clear();
    }
  }

  @Test
  public void testUndo() {
    final int pageSize = 64 * 1024;

    final OByteBufferPool byteBufferPool = new OByteBufferPool(pageSize);
    try {
      final OPointer pointer = byteBufferPool.acquireDirect(false);
      final OCachePointer cachePointer = new OCachePointer(pointer, byteBufferPool, 0, 0);
      final OCacheEntry entry = new OCacheEntryImpl(0, 0, cachePointer, false);

      CellBTreeSingleValueEntryPointV1 bucket = new CellBTreeSingleValueEntryPointV1(entry);
      bucket.init();

      bucket.setPagesSize(42);

      entry.clearPageOperations();

      bucket.setPagesSize(24);

      final List<PageOperationRecord> operations = entry.getPageOperations();
      Assert.assertEquals(1, operations.size());

      Assert.assertTrue(
          operations.get(0) instanceof CellBTreeEntryPointSingleValueV1SetPagesSizePO);

      final CellBTreeEntryPointSingleValueV1SetPagesSizePO pageOperation =
          (CellBTreeEntryPointSingleValueV1SetPagesSizePO) operations.get(0);

      final CellBTreeSingleValueEntryPointV1 restoredBucket =
          new CellBTreeSingleValueEntryPointV1(entry);

      Assert.assertEquals(24, restoredBucket.getPagesSize());

      pageOperation.undo(entry);

      Assert.assertEquals(42, restoredBucket.getPagesSize());

      byteBufferPool.release(pointer);
    } finally {
      byteBufferPool.clear();
    }
  }

  @Test
  public void testSerialization() {
    CellBTreeEntryPointSingleValueV1SetPagesSizePO operation =
        new CellBTreeEntryPointSingleValueV1SetPagesSizePO(42, 24);

    operation.setFileId(42);
    operation.setPageIndex(24);
    operation.setOperationUnitId(1);

    final int serializedSize = operation.serializedSize();
    final byte[] stream = new byte[serializedSize + 1];
    int pos = operation.toStream(stream, 1);

    Assert.assertEquals(serializedSize + 1, pos);

    CellBTreeEntryPointSingleValueV1SetPagesSizePO restoredOperation =
        new CellBTreeEntryPointSingleValueV1SetPagesSizePO();
    restoredOperation.fromStream(stream, 1);

    Assert.assertEquals(42, restoredOperation.getFileId());
    Assert.assertEquals(24, restoredOperation.getPageIndex());
    Assert.assertEquals(1, restoredOperation.getOperationUnitId());

    Assert.assertEquals(42, restoredOperation.getPrevPagesSize());
    Assert.assertEquals(24, restoredOperation.getPagesSize());
  }
}
