package com.j256.simplewebframework.cms;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.j256.simplejmx.common.JmxAttributeField;
import com.j256.simplejmx.common.JmxOperation;
import com.j256.simplejmx.common.JmxResource;
import com.j256.simplewebframework.logger.Logger;
import com.j256.simplewebframework.logger.LoggerFactory;
import com.j256.simplewebframework.util.IoUtils;

/**
 * Amazon S3 Content Source to get the CMS revision information from S3. To use this class you will need to depend on
 * the AWS jars.
 * 
 * <p>
 * The revision config file is downloaded from {@code s3.amazonaws.com/s3Bucket/s3CmsPrefix/configPath}. The
 * revision-zip is downloaded from {@code s3.amazonaws.com/s3Bucket/s3CmsPrefix/branch/}.
 * </p>
 * 
 * @author graywatson
 */
@JmxResource(domainName = "j256.simpleweb", description = "CMS downloader and manager")
public class AmazonS3ContentSource implements ContentSource {

	private static final Logger logger = LoggerFactory.getLogger(AmazonS3ContentSource.class);

	@JmxAttributeField(description = "s3 bucket to download from")
	private String s3Bucket;
	@JmxAttributeField(description = "prefix in the bucket to prepend to all paths")
	private String s3CmsPrefix;
	@JmxAttributeField(description = "path to the configuration file after prefix")
	private String configPath;

	private AmazonS3Client s3Client;

	@Override
	public InputStream getRevisionConfigInputStream() throws IOException {
		String s3Path = s3CmsPrefix + "/" + configPath;
		// download the config file
		S3Object object = getObject(s3Bucket, s3Path);
		if (object == null) {
			logger.error("Could not find config-file '" + s3Path + "' in bucket '" + s3Bucket + "'");
			return null;
		}
		return object.getObjectContent();
	}

	@Override
	public InputStream getContentZipInputStream(String branch, int revision) throws IOException {
		String cmsZipPath = s3CmsPrefix + "/" + branch + "/" + revision + ".zip";
		S3Object object = getObject(s3Bucket, cmsZipPath);
		if (object == null) {
			logger.error("Could not find cms zip file: " + cmsZipPath);
			return null;
		}
		return object.getObjectContent();
	}

	public void setS3Bucket(String s3Bucket) {
		this.s3Bucket = s3Bucket;
	}

	public void setS3CmsPrefix(String s3CmsPrefix) {
		this.s3CmsPrefix = s3CmsPrefix;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

	public void setAwsCredentials(AWSCredentials awsCredentials) {
		this.s3Client = new AmazonS3Client(awsCredentials);
	}

	@JmxOperation(description = "Show the contents of the revision config file")
	public String downloadRevisionConfig() {
		InputStream stream = null;
		InputStreamReader reader = null;
		try {
			stream = getRevisionConfigInputStream();
			if (stream == null) {
				return "Config file not found";
			}
			reader = new InputStreamReader(stream);
			stream = null;
			StringWriter writer = new StringWriter();
			char[] buf = new char[1024];
			while (true) {
				int numRead = reader.read(buf);
				if (numRead < 0) {
					break;
				}
				writer.write(buf, 0, numRead);
			}
			return writer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "Threw " + e;
		} finally {
			IoUtils.closeQuietly(reader);
			IoUtils.closeQuietly(stream);
		}
	}

	private S3Object getObject(String bucketName, String key) throws IOException {
		try {
			return s3Client.getObject(new GetObjectRequest(bucketName, key));
		} catch (AmazonServiceException e) {
			logger.error(e, "getting '" + key + "' from bucket '" + bucketName + "' threw: " + e);
			if (e.getStatusCode() == 404 /* not found */
					|| e.getStatusCode() == 403 /* forbidden */) {
				return null;
			} else {
				throw new IOException("Could not get bucket '" + bucketName + "', key '" + key + "'", e);
			}
		} catch (AmazonClientException e) {
			throw new IOException("Could not get bucket '" + bucketName + "', key '" + key + "'", e);
		}
	}
}
