-- =========================================================================================
-- TẬP TIN DỰ PHÒNG CÁC BẢNG TIẾNG ANH THỪA (TỒN TẠI TỪ 04/04/2026)
-- ĐƯỢC BACKUP TRƯỚC KHI DỌN DẸP CƠ SỞ DỮ LIỆU QuanLyGuiXe
-- =========================================================================================

-- 1. BẢNG users
CREATE TABLE [dbo].[users] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [username] NVARCHAR(255) NULL,
    [email] NVARCHAR(255) NULL,
    [password] NVARCHAR(255) NULL,
    [role] NVARCHAR(50) NULL
);
INSERT INTO [dbo].[users] ([username], [email], [password], [role]) VALUES 
(N'admin', N'admin@gmail.com', N'123456', N'ADMIN'),
(N'user1', N'user1@gmail.com', N'123456', N'USER');

-- 2. BẢNG posts
CREATE TABLE [dbo].[posts] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [title] NVARCHAR(255) NULL,
    [content] NVARCHAR(MAX) NULL,
    [image] NVARCHAR(255) NULL,
    [steam_url] NVARCHAR(255) NULL,
    [likes] INT NULL,
    [created_at] DATETIME2 NULL
);

-- 3. BẢNG post_likes
CREATE TABLE [dbo].[post_likes] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [post_id] BIGINT NULL,
    [username] VARCHAR(255) NULL
);

-- 4. BẢNG comments
CREATE TABLE [dbo].[comments] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [post_id] BIGINT NULL,
    [user_id] BIGINT NULL,
    [parent_id] BIGINT NULL,
    [content] NVARCHAR(MAX) NULL,
    [created_at] DATETIME2 NULL
);

-- 5. BẢNG videos
CREATE TABLE [dbo].[videos] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [title] NVARCHAR(255) NULL,
    [video_url] NVARCHAR(255) NULL,
    [thumbnail] NVARCHAR(255) NULL
);

-- 6. BẢNG contacts
CREATE TABLE [dbo].[contacts] (
    [id] BIGINT IDENTITY(1,1) PRIMARY KEY,
    [name] NVARCHAR(255) NULL,
    [email] NVARCHAR(255) NULL,
    [message] NVARCHAR(MAX) NULL,
    [created_at] DATETIME2 NULL
);
