package sa.com.is.ui.messageview;


import sa.com.is.mailstore.AttachmentViewInfo;


interface AttachmentViewCallback {
    void onViewAttachment(AttachmentViewInfo attachment);
    void onSaveAttachment(AttachmentViewInfo attachment);
    void onSaveAttachmentToUserProvidedDirectory(AttachmentViewInfo attachment);
}
