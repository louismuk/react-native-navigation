package com.reactnativenavigation.views.sharedElementTransition;

import android.annotation.TargetApi;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.widget.TextView;

import com.facebook.react.views.image.ReactImageView;
import com.reactnativenavigation.params.InterpolationParams;
import com.reactnativenavigation.params.PathInterpolationParams;
import com.reactnativenavigation.params.parsers.SharedElementTransitionParams;
import com.reactnativenavigation.utils.ViewUtils;
import com.reactnativenavigation.views.utils.ImageUtils;
import com.reactnativenavigation.views.utils.Point;

public class AnimatorValuesResolver {

    final Point fromXy;
    final Point toXy;
    final int fromLeft;
    final int toLeft;
    final int fromTop;
    final int toTop;
    final int fromBottom;
    final int toBottom;
    final int toWidth;
    final int toHeight;
    final Matrix fromMatrix;
    final Matrix toMatrix;
    final float startScaleX;
    final float endScaleX;
    final float startScaleY;
    final float endScaleY;
    int dx;
    int dy;
    int startX;
    int startY;
    int endX;
    int endY;
    float controlX1;
    float controlY1;
    float controlX2;
    float controlY2;
    int startColor;
    int endColor;
    Rect startDrawingRect = new Rect();
    Rect endDrawingRect = new Rect();

    AnimatorValuesResolver(SharedElementTransition from, SharedElementTransition to, SharedElementTransitionParams params) {
        fromXy = calculateFromXY(from, to, params);
        toXy = calculateToXY(to, from, params);
        fromLeft = from.getLeft();
        toLeft = to.getLeft();
        fromTop = from.getTop();
        toTop = to.getTop();
        fromBottom = from.getBottom();
        toBottom = to.getBottom();
        toWidth = to.getWidth();
        toHeight = to.getHeight();
        startScaleX = calculateStartScaleX(from, to);
        endScaleX = calculateEndScaleX(from, to);
        startScaleY = calculateStartScaleY(from, to);
        endScaleY = calculateEndScaleY(from, to);
        calculateColor(from, to);
        calculate(params.interpolation);
        calculateDrawingReacts(from, to);
        fromMatrix = calculateMatrix(from);
        toMatrix = calculateMatrix(to);
    }

    private Point calculateFromXY(SharedElementTransition from, SharedElementTransition to, SharedElementTransitionParams params) {
        Point loc = ViewUtils.getLocationOnScreen(from.getSharedView());
        if (params.animateClipBounds) {
            if (from.getHeight() != to.getHeight()) {
                if (from.getHeight() < to.getHeight()) {
                    loc.y -= (to.getHeight() - from.getHeight()) / 2;
                }
            }
        }
        return loc;
    }

    private Point calculateToXY(SharedElementTransition to, SharedElementTransition from, SharedElementTransitionParams params) {
        Point loc = ViewUtils.getLocationOnScreen(to.getSharedView());
        if (params.animateClipBounds) {
            if (from.getHeight() != to.getHeight()) {
                if (from.getHeight() > to.getHeight()) {
                    loc.y -= (from.getHeight() - to.getHeight()) / 2;
                }
            }
        }
        return loc;
    }


    protected float calculateEndScaleY(SharedElementTransition from, SharedElementTransition to) {
        return 1;
    }

    protected float calculateStartScaleY(SharedElementTransition from, SharedElementTransition to) {
        return ((float) from.getHeight()) / to.getHeight();
    }

    protected float calculateEndScaleX(SharedElementTransition from, SharedElementTransition to) {
        return 1;
    }

    protected float calculateStartScaleX(SharedElementTransition from, SharedElementTransition to) {
        return ((float) from.getWidth()) / to.getWidth();
    }

    private void calculate(InterpolationParams interpolation) {
        calculateDeltas();
        calculateStartPoint();
        calculateEndPoint();
        if (interpolation instanceof PathInterpolationParams) {
            calculateControlPoints((PathInterpolationParams) interpolation);
        }
    }

    protected void calculateDeltas() {
        dx = fromXy.x - toXy.x;
        dy = fromXy.y - toXy.y;
    }

    protected void calculateEndPoint() {
        endX = 0;
        endY = 0;
    }

    protected void calculateStartPoint() {
        startX = dx;
        startY = dy;
    }

    protected void calculateControlPoints(PathInterpolationParams interpolation) {
        controlX1 = dx * interpolation.p1.x;
        controlY1 = dy * interpolation.p1.y;
        controlX2 = dx * interpolation.p2.x;
        controlY2 = dy * interpolation.p2.y;
    }

    private void calculateColor(SharedElementTransition from, SharedElementTransition to) {
        if (from.getSharedView() instanceof TextView && to.getSharedView() instanceof TextView) {
            startColor = ViewUtils.getForegroundColorSpans((TextView) from.getSharedView())[0].getForegroundColor();
            endColor = ViewUtils.getForegroundColorSpans((TextView) to.getSharedView())[0].getForegroundColor();
        }
    }

    private void calculateDrawingReacts(SharedElementTransition from, SharedElementTransition to) {
        from.getDrawingRect(startDrawingRect);
        to.getDrawingRect(endDrawingRect);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private Matrix calculateMatrix(SharedElementTransition view) {
        if (!(view.getSharedView() instanceof ReactImageView)) {
            return new Matrix();
        }

        ReactImageView imageView = (ReactImageView) view.getSharedView();
        RectF r = new RectF();
        imageView.getHierarchy().getActualImageBounds(r);

        return ImageUtils.getScaleType(imageView).getTransform(
                new Matrix(),
                new Rect(0, 0, view.getWidth(), view.getHeight()),
                1002,
                499,
                0 ,0
                );
    }
}