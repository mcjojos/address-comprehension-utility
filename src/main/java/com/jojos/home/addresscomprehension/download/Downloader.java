/*
 * Copyright (c) 2016. All Rights Reserved
 */

package com.jojos.home.addresscomprehension.download;

/**
 * As the name suggests this class defines the interface to download content over the internet.
 * <p>
 * Created by karanikasg@gmail.com.
 *
 */
public interface Downloader<T extends DownloaderCtx, R> {

    R download(T context);

}
