package sprout.clipcon.server.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import lombok.Setter;
import sprout.clipcon.server.controller.Server;
import sprout.clipcon.server.controller.UploadServlet;

@Setter
public class Evaluation {
	public static long uploadStartTime = 0;
	public static long uploadEndTime = 0;
	public static String multipleContentsInfo = "";
	public static long downloadStartTime = 0;
	public static long downloadEndTimeBeforeCompress = 0;
	public static long downloadEndTimeAfterCompress = 0;

	/** Export the log data to a file. */
	public static void createEvaluationFile(String evaluationType, String groupPK, String userName, String deviceType,
			long contentsLength, String contentsType) {
		InetAddress local;
		String logdata;
		String outputFileName = null;
		long startTime = 0;
		String endTime = null;

		if (evaluationType.equals("UPLOAD")) {
			outputFileName = "logUploadInfoData.txt";
			startTime = uploadStartTime;
			endTime = uploadEndTime + "";
		} else if (evaluationType.equals("DOWNLOAD")) {
			outputFileName = "logDownloadInfoData.txt";
			startTime = downloadStartTime;
			endTime = downloadEndTimeBeforeCompress + ", " + downloadEndTimeAfterCompress;
		}

		// float contentLengthToKB = ((float) contentsLength / 1024);
		// float time = (float) (uploadEndTime - uploadStartTime) / 1000;
		// float speed = contentLengthToKB / time;

		try {
			local = InetAddress.getLocalHost();

			FileWriter fw = new FileWriter(Server.SERVER_ROOT_LOCATION + outputFileName, true);
			BufferedWriter bw = new BufferedWriter(fw);

			logdata = groupPK + "\t";
			logdata += userName + "\t";
			logdata += UploadServlet.uploadTime() + "\t";
			logdata += local.getHostAddress() + "\t";
			logdata += local.getHostName() + "\t";
			logdata += startTime + "\t";
			logdata += endTime + "\t";
			if (!deviceType.equals("pcProgram")) {
				deviceType = "androidProgram";
			}
			logdata += deviceType + "\t";
			logdata += contentsLength + "\t";
			logdata += contentsType + "\t";
			logdata += multipleContentsInfo;

			// System.out.println("\n[LOG] ");
			// /* contents uploadTime */
			// System.out.println("Time = " + time + "sec");
			// System.out.print("Speed = " + speed + " kb/s (" + speed / 1024 + " mb/s)\n");

			bw.write(logdata);
			bw.newLine();
			bw.flush();
			bw.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(e); // 에러가 있다면 메시지 출력
		}
	}
}
