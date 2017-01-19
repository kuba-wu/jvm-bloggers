package com.jvm_bloggers.frontend.admin_area.moderation

import com.jvm_bloggers.MockSpringContextAwareSpecification
import com.jvm_bloggers.common.utils.NowProvider
import com.jvm_bloggers.entities.blog_posts.Blog
import com.jvm_bloggers.entities.blog_posts.BlogPost
import com.jvm_bloggers.entities.blog_posts.BlogPostRepository
import com.jvm_bloggers.entities.blog_posts.BlogType
import com.jvm_bloggers.frontend.admin_area.blogs.BlogPostToModerateModel
import org.apache.wicket.behavior.AttributeAppender
import org.apache.wicket.markup.repeater.Item
import spock.lang.Subject

import java.time.LocalDateTime
import java.time.Month

class BlogPostItemPopulatorSpec extends MockSpringContextAwareSpecification {

    static LocalDateTime SATURDAY_19TH_12_00 = LocalDateTime.of(2016, Month.MARCH, 19, 12, 0, 0)

    NowProvider nowProvider = Stub(NowProvider) {
        now() >> SATURDAY_19TH_12_00
    }

    @Subject
    private BlogPostItemPopulator blogPostItemPopulator = new BlogPostItemPopulator(nowProvider)

    def "Should highlight post going in newsletter"() {
        given:
            BlogPost blogPost = createBlogPostAcceptedOn(SATURDAY_19TH_12_00.plusDays(1))
            Item<BlogPost> item = createBlogPostItem(blogPost)
        when:
            blogPostItemPopulator.populateItem(item, null, null)
        then:
            item.getBehaviors(AttributeAppender).any { isHighlighted(it) }
    }

    def "Should not highlight post not going in newsletter"() {
        given:
            BlogPost blogPost = createBlogPostAcceptedOn(SATURDAY_19TH_12_00.minusDays(1))
            Item<BlogPost> item = createBlogPostItem(blogPost)
        when:
            blogPostItemPopulator.populateItem(item, null, null)
        then:
            !(item.getBehaviors(AttributeAppender).any { isHighlighted(it) })
    }

    private boolean isHighlighted(AttributeAppender attributeAppender) {
        return attributeAppender.getAttribute() == "class" && attributeAppender.replaceModel.getObject() == "highlighted-post"
    }

    private BlogPost createBlogPostAcceptedOn(LocalDateTime acceptationDate) {
        Blog blog = Blog.builder()
                .jsonId(0L)
                .author("Blog author")
                .rss("rss")
                .url("url")
                .dateAdded(nowProvider.now())
                .blogType(BlogType.PERSONAL)
                .build()
        return BlogPost.builder()
                .blog(blog)
                .approved(Boolean.TRUE)
                .publishedDate(nowProvider.now())
                .approvedDate(acceptationDate)
                .title("title")
                .url("url")
                .build()
    }

    private Item<BlogPost> createBlogPostItem(BlogPost blogPost) {
        return new Item<>("itemId", 1, new BlogPostToModerateModel(blogPost))
    }

    @Override
    protected void setupContext() {
        addBean(Stub(BlogPostRepository))
        addBean(nowProvider)
    }
}
