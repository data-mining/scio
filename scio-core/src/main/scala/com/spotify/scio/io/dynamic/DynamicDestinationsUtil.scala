/*
 * Copyright 2017 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.scio.io.dynamic

import org.apache.avro.Schema
import org.apache.beam.sdk.io.FileBasedSink.DynamicDestinations
import org.apache.beam.sdk.io._
import org.apache.beam.sdk.options.ValueProvider

private[dynamic] object DynamicDestinationsUtil {

  def fileFn[T](path: String, suffix: String, windowedWrites: Boolean,
                 defaultDestination: String, destinationFn: T => String)
  : DynamicDestinations[T, String, T] = new DynamicDestinations[T, String, T] {
    override def formatRecord(record: T): T = record
    override def getDestination(element: T): String = destinationFn(element)
    override def getDefaultDestination: String = defaultDestination
    override def getFilenamePolicy(destination: String): FileBasedSink.FilenamePolicy = {
      val prefix = s"$path/$destination/part" + (if (windowedWrites) "-" else "")
      DefaultFilenamePolicy.fromStandardParameters(
        ValueProvider.StaticValueProvider.of(
          FileSystems.matchNewResource(prefix, false)),
        null, suffix, windowedWrites)
    }
  }

  def avroFn[T](path: String, suffix: String, windowedWrites: Boolean,
                defaultDestination: String, destinationFn: T => String,
                schema: Schema)
  : DynamicAvroDestinations[T, String, T] = new DynamicAvroDestinations[T, String, T] {
    override def formatRecord(record: T): T = record
    override def getDestination(element: T): String = destinationFn(element)
    override def getDefaultDestination: String = defaultDestination
    override def getFilenamePolicy(destination: String): FileBasedSink.FilenamePolicy = {
      val prefix = s"$path/$destination/part" + (if (windowedWrites) "-" else "")
      DefaultFilenamePolicy.fromStandardParameters(
        ValueProvider.StaticValueProvider.of(
          FileSystems.matchNewResource(prefix, false)),
        null, suffix, windowedWrites)
    }
    override def getSchema(destination: String) = schema
  }

}
