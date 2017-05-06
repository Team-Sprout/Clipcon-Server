package sprout.clipcon.server.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class History {
	@Getter
	@Setter
	private String groupPK;
	private Map<String, Contents> contentsMap;

	// 그룹내의 각 Data를 구분하는 고유키값
	private static int contentsPKValue = 0;

	public History(String groupPK) {
		this.groupPK = groupPK; // XXX[delf]: 사실 필요한지 잘 모르겠음
		contentsMap = new HashMap<String, Contents>();
	}

	/** 새로운 데이터가 업로드되면 히스토리에 add한 후 Contents를 return */
	public Contents addContents(Contents contents) {
		contents.setContentsPKName(Integer.toString(++contentsPKValue));
		System.out.println("History addContents ContentsPkName: " + contents.getContentsPKName());
		contentsMap.put(contents.getContentsPKName(), contents);

		return contentsMap.get(contents.getContentsPKName());
	}

	/** Data를 구분하는 고유키값과 일치하는 Contents를 return */
	public Contents getContentsByPK(String contentsPKName) {
		return contentsMap.get(contentsPKName);
	}
}
