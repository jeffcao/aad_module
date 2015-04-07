package com.yyxu.download.services;

interface IDownloadService {
	
	void startManage();
	
	void addTask(String url, String name);
	
	void pauseTask(String url);
	
	void deleteTask(String url);
	
	void continueTask(String url);
}
