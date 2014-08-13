package org.magnum.dataup;

import org.magnum.dataup.exception.WebError;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class VideoController {

    private List<Video> videos = new ArrayList<Video>();
    private AtomicLong id = new AtomicLong();

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
    public @ResponseBody Collection<Video> getVideoList() {
        return videos;
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
    public @ResponseBody Video addVideo(@RequestBody Video v) {
        v.setId(id.incrementAndGet());
        if(v.getDataUrl() == null){
            v.setDataUrl("/video/" + v.getId());
        }
        videos.add(v);
        return v;
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.POST)
    public
    @ResponseBody
    VideoStatus setVideoData(@PathVariable(value = VideoSvcApi.ID_PARAMETER) long id, @RequestPart(VideoSvcApi.DATA_PARAMETER) MultipartFile videoData) throws IOException {
        for (Video video : videos) {
            if (video.getId() == id) {
                VideoFileManager.get().saveVideoData(video, videoData.getInputStream());
                return new VideoStatus(VideoStatus.VideoState.READY);
            }
        }
        throw new WebError("wrong id");
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.GET)
    public void getData(HttpServletResponse response, @PathVariable(VideoSvcApi.ID_PARAMETER) long id) throws IOException {
        for (Video video : videos) {
            if (video.getId() == id) {
                if(VideoFileManager.get().hasVideoData(video)){
                    VideoFileManager.get().copyVideoData(video, response.getOutputStream());
                }
            }
        }
        throw new WebError("wrong id");
    }
}
