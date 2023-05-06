$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	// 获取标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	// 发送异步请求
	// 同时随机生成token，用于设置Cookie和表单
	var token = uuid();
	document.cookie = "_csrf=" + token;
	console.log(document.cookie);
	$.post(
		CONTEXT_PATH + "/discuss/add",
		{"title": title, "content": content, "_csrf": token},
		function (data) {
			data = $.parseJSON(data);
			// 在提示框当中显示返回消息
			$("#hintBody").text(data.msg);
			// 显示提示框
			$("#hintModal").modal("show");
			// 2秒后隐藏
			setTimeout(
				function(){
					$("#hintModal").modal("hide");
					// 刷新页面
					if (data.code === 0) {
						window.location.reload();
					}
				},
				2000
			);
		}
	);
}

function uuid() {
	var s = [];
	var hexDigits = "0123456789abcdef";
	for (var i = 0; i < 36; i++) {
		s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
	}
	s[14] = "4"; // bits 12-15 of the time_hi_and_version field to 0010
	s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1); // bits 6-7 of the clock_seq_hi_and_reserved to 01
	s[8] = s[13] = s[18] = s[23] = "-";

	var uuid = s.join("");
	return uuid;
}