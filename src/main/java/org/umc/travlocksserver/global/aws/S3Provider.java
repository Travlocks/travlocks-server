package org.umc.travlocksserver.global.aws;

import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.umc.travlocksserver.global.code.ErrorCode;
import org.umc.travlocksserver.global.exception.handler.S3ExceptionHandler;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
public class S3Provider {

	private final S3Client s3Client;
	private final S3Properties s3Properties;

	public String uploadVlockFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return null;
		}

		String fileName = "vlocks/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(s3Properties.bucket())
				.key(fileName)
				.contentType(file.getContentType())
				.build();

			s3Client.putObject(putObjectRequest,
				RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
		} catch (IOException e) {
			throw new S3ExceptionHandler(ErrorCode.FILE_READ_ERROR);
		} catch (S3Exception e) {
			throw new S3ExceptionHandler(ErrorCode.S3_UPLOAD_FAIL);
		}

		return String.format(s3Properties.domain() + "%s", fileName);
	}

	public void deleteFile(String fileUrl) {
		if (fileUrl == null || !fileUrl.contains(s3Properties.bucket())) {
			return;
		}

		String key = fileUrl.substring(fileUrl.lastIndexOf(".com/") + 5);

		try {
			DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
				.bucket(s3Properties.bucket())
				.key(key)
				.build();

			s3Client.deleteObject(deleteObjectRequest);
		} catch (S3Exception e) {
			throw new S3ExceptionHandler(ErrorCode.S3_DELETE_FAIL);
		}
	}

	public String uploadTemplateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return null;
		}

		String fileName = "templates/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(s3Properties.bucket())
				.key(fileName)
				.contentType(file.getContentType())
				.build();

			s3Client.putObject(putObjectRequest,
				RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
		} catch (IOException e) {
			throw new S3ExceptionHandler(ErrorCode.FILE_READ_ERROR);
		} catch (S3Exception e) {
			throw new S3ExceptionHandler(ErrorCode.S3_UPLOAD_FAIL);
		}

		return String.format(s3Properties.domain() + "%s", fileName);
	}
}
